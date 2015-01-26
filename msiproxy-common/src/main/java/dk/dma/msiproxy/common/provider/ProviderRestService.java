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
package dk.dma.msiproxy.common.provider;

import dk.dma.msiproxy.model.MessageFilter;
import dk.dma.msiproxy.model.msi.Message;
import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides a REST interface for accessing provider-specific MSI messages.
 * <p>
 * The provider part of the URI may either be a single provider ID or a
 * colon-separated list of provider ID's
 */
@Path("/{provider}/v1/service")
public class ProviderRestService {

    @Inject
    Logger log;

    @Inject
    Providers providers;

    @Context
    Request request;

    /**
     * Returns the active MSI messages in the requested format and language.
     *
     * @param provider the provider(s)
     * @param refresh whether to force a refresh of the data or not
     * @param format either "json" (default) or "xml"
     * @param lang the requested language, either "da" (default) or "en"
     * @param details whether to include message details or not
     * @param types comma-separated list of message types to include
     * @param areaId the id of an area to filter the messages on
     * @param categoryId the id of a category to filter the messages on
     * @return the active MSI messages filtered according to the parameters
     */
    @GET
    @Path("/messages")
    @GZIP
    public Response getActiveMessages(
            @PathParam("provider")      String provider,
            @QueryParam("refresh")      @DefaultValue("false") boolean refresh,
            @QueryParam("format")       @DefaultValue("json") String format,
            @QueryParam("lang")         @DefaultValue("da") String lang,
            @QueryParam("details")      @DefaultValue("true") boolean details,
            @QueryParam("types")        String types,
            @QueryParam("areaId")       Integer areaId,
            @QueryParam("categoryId")   Integer categoryId
    ) {

        try {

            // The provider is either a single provider, or a colon-separated list of providers
            List<AbstractProviderService> providerServices = providers.getProviders(provider);

            // Sanity check
            if (providerServices.size() == 0) {
                log.error("No valid provider specified: " + provider);
                return Response.status(404).build();
            }

            // Check if we need to refresh the data
            if (refresh) {
                providerServices.stream()
                        .forEach(AbstractProviderService::loadMessages);
            }

            // Compute expiry 10 min from now
            Date expirationDate = new Date(System.currentTimeMillis() + 10L * 60L * 1000L);

            // Compute the content type
            String messageFormat = "xml".equalsIgnoreCase(format) ? "xml" : "json";
            String contentType = String.format("application/%s;charset=UTF-8", format);

            // Compose the filter to filter the messages by
            MessageFilter filter = new MessageFilter()
                    .lang(lang)
                    .detailed(details)
                    .area(areaId)
                    .category(categoryId)
                    .types(types == null ? null : types.split(","));

            // Fetch the filtered list of message from the provider(s).
            List<Message> messages = new ArrayList<>();
            StringBuilder etagToken = new StringBuilder();
            providerServices.stream()
                    .forEach(p -> {
                        List<Message> providerMessages = p.getCachedMessages(filter);
                        messages.addAll(providerMessages);
                        etagToken.append(p.getETagToken(messageFormat, filter, messages)).append(":");
                    });

            // Check for an ETag match
            EntityTag etag = new EntityTag(etagToken.toString(), true);
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
                    .ok(new GenericEntity<List<Message>>(messages) {
                    }, contentType)
                    .expires(expirationDate)
                    .tag(etag)
                    .build();


        } catch (Exception e) {
            log.error("Failed loading active messages: " + e.getMessage());
            return Response.status(500).build();
        }
    }
}
