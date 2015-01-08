package dk.dma.msiproxy.dkprovider.service;

import dk.dma.msiproxy.model.MessageFilter;
import dk.dma.msiproxy.model.msi.Message;
import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * Provides a REST interface for accessing Danish legacy MSI messages
 */
@Path("/" + DkProviderService.PROVIDER_ID + "/v1/service")
public class DkProviderRestService {

    @Inject
    Logger log;

    @Inject
    DkProviderService providerService;

    @Context
    Request request;

    /**
     * Returns the active MSI messages in the requested format and language.
     *
     * @param format either "json" (default) or "xml"
     * @param lang the requested language, either "da" (default) or "en"
     * @return the active MSI messages
     */
    @GET
    @Path("/messages")
    @GZIP
    public Response getActiveMessages(
            @QueryParam("format") @DefaultValue("json") String format,
            @QueryParam("lang") @DefaultValue("da") String lang,
            @QueryParam("details") @DefaultValue("true") boolean details,
            @QueryParam("types") String types,
            @QueryParam("areaId") Integer areaId,
            @QueryParam("categoryId") Integer categoryId
    ) {

        try {
            MessageFilter filter = new MessageFilter()
                    .lang(lang)
                    .detailed(details)
                    .area(areaId)
                    .category(categoryId)
                    .types(types == null ? null : types.split(","));

            List<Message> messages = providerService.getCachedMessages(filter);

            // Compute expiry 10 min from now
            Date expirationDate = new Date(System.currentTimeMillis() + 10L * 60L * 1000L);

            // Compute the content type
            format = "xml".equalsIgnoreCase(format) ? "xml" : "json";
            String contentType = String.format("application/%s;charset=UTF-8", format);

            // Check for an ETag match
            EntityTag etag = new EntityTag(providerService.getETagToken(format, filter, messages), true);
            Response.ResponseBuilder responseBuilder = request.evaluatePreconditions(etag);
            if (responseBuilder != null) {
                // ETag match
                log.info("Message list unchanged. Return code 304");
                return responseBuilder
                        .expires(expirationDate)
                        .build();
            }

            log.info("Returning message list");
            return Response
                    .ok(new GenericEntity<List<Message>>(messages) {}, contentType)
                    .expires(expirationDate)
                    .tag(etag)
                    .build();


        } catch (Exception e) {
            log.error("Failed loading active messages: " + e.getMessage());
            throw new WebApplicationException("Error loading messages: " + e.getMessage(), 500);
        }
    }

}
