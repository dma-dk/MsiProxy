/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msiproxy.web;

import com.itextpdf.text.DocumentException;
import dk.dma.msiproxy.common.MsiProxyApp;
import dk.dma.msiproxy.common.provider.AbstractProviderService;
import dk.dma.msiproxy.common.provider.Providers;
import dk.dma.msiproxy.common.util.WebUtils;
import dk.dma.msiproxy.model.MessageFilter;
import dk.dma.msiproxy.model.msi.Message;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Servlet used for generating either a HTML details page
 * or a PDF for the MSI details defined by the provider and language
 * specified using request parameters.
 */
@WebServlet(urlPatterns = {"/details.pdf", "/details.html"}, asyncSupported = true)
public class MessageDetailsServlet extends HttpServlet {

    private static final String DETAILS_JSP_FILE = "/WEB-INF/jsp/details.jsp";

    @Inject
    Logger log;

    @Inject
    Providers providers;

    @Inject
    MsiProxyApp app;

    /**
     * Main GET method
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // Never cache the response
        response = WebUtils.nocache(response);

        // Read the provider and language parameters
        String providerId = request.getParameter("provider");
        String lang = request.getParameter("lang");
        String messageId = request.getParameter("messageId");

        List<AbstractProviderService> providerServices = providers.getProviders(providerId);

        if (providerServices.size() == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid 'provider' parameter");
            return;
        }

        // Force the encoding and the locale based on the lang parameter
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        final Locale locale = new Locale(lang);
        request = new HttpServletRequestWrapper(request) {
            @Override public Locale getLocale() { return locale; }
        };

        // Get the messages in the given language for the requested provider
        MessageFilter filter = new MessageFilter().lang(lang);
        Integer id = StringUtils.isNumeric(messageId) ? Integer.valueOf(messageId) : null;
        List<Message> messages = providerServices.stream()
                .flatMap(p -> p.getCachedMessages(filter).stream())
                .filter(msg -> (id == null || id.equals(msg.getId())))
                .collect(Collectors.toList());

        // Register the attributes to be used on the JSP apeg
        request.setAttribute("messages", messages);
        request.setAttribute("baseUri", app.getBaseUri());
        request.setAttribute("lang", lang);
        request.setAttribute("locale", locale);
        request.setAttribute("provider", providerId);

        if (request.getServletPath().endsWith("pdf")) {
            generatePdfFile(request, response);
        } else {
            generateHtmlPage(request, response);
        }
    }

    /**
     * Generates a HTML page containing the MSI message details
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     */
    private void generateHtmlPage(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Normal processing
        request.getRequestDispatcher(DETAILS_JSP_FILE).include(request, response);
        response.flushBuffer();
    }

    /**
     * Generates a PDF file containing the MSI message details
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     */
    private void generatePdfFile(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        //Capture the content for this request
        ContentCaptureServletResponse capContent = new ContentCaptureServletResponse(response);
        request.getRequestDispatcher(DETAILS_JSP_FILE).include(request, capContent);

        // Check if there is content. Could be a redirect...
        if (!capContent.hasContent()) {
            return;
        }

        try {
            // Clean up the response HTML to a document that is readable by the XHTML renderer.
            String content = capContent.getContent();
            Document xhtmlContent = cleanHtml(content);

            long t0 = System.currentTimeMillis();
            String baseUri = app.getBaseUri();
            log.info("Generating PDF for " + baseUri);

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocument(xhtmlContent, baseUri);
            renderer.layout();

            response.setContentType("application/pdf");
            if (StringUtils.isNotBlank(request.getParameter("attachment"))) {
                response.setHeader("Content-Disposition", "attachment; filename=" + request.getParameter("attachment"));
            }
            OutputStream browserStream = response.getOutputStream();
            renderer.createPDF(browserStream);

            log.info("Completed PDF generation in " + (System.currentTimeMillis() - t0) + " ms");
        } catch (DocumentException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Use JTidy to clean up the HTML
     * @param html the HTML to clean up
     * @return the resulting XHTML
     */
    public Document cleanHtml(String html) {
        Tidy tidy = new Tidy();

        tidy.setShowWarnings(false); //to hide errors
        tidy.setQuiet(true); //to hide warning

        tidy.setXHTML(true);
        return tidy.parseDOM(new StringReader(html), new StringWriter());
    }

    /**
     * Response wrapper
     * Collects all contents
     */
    public static class ContentCaptureServletResponse extends HttpServletResponseWrapper {

        private StringWriter contentWriter;
        private PrintWriter writer;

        /**
         * Constructor
         * @param originalResponse the original response
         */
        public ContentCaptureServletResponse(HttpServletResponse originalResponse) {
            super(originalResponse);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            throw new IllegalStateException("Call getWriter()");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public PrintWriter getWriter() throws IOException {
            if(writer == null){
                contentWriter = new StringWriter();
                writer = new PrintWriter(contentWriter);
            }
            return writer;
        }

        /**
         * Returns if the response contains content
         * @return if the response contains content
         */
        public boolean hasContent() {
            return (writer != null);
        }

        /**
         * Returns the contents of the response as a string
         * @return the contents of the response as a string
         */
        public String getContent(){
            if (writer == null) {
                return "<html/>";
            }
            writer.flush();
            return contentWriter.toString();
        }
    }
}
