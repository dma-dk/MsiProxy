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
import org.slf4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * Provides a base class for the REST interface for accessing provider specific MSI messages
 */
public abstract class AbstractProviderRestService {

    /**
     * Returns a reference to the current provider service
     * @return a reference to the current provider service
     */
    public abstract AbstractProviderService getProviderService();

    /**
     * Returns the current logger
     * @return the current logger
     */
    public abstract Logger getLog();

    /**
     * Returns the active MSI messages in the requested format and language.
     *
     * @param request the JAX-RS request
     * @param refresh whether to force a refresh of the data or not
     * @param format either "json" (default) or "xml"
     * @param lang the requested language, either "da" (default) or "en"
     * @param details whether to include message details or not
     * @param types comma-separated list of message types to include
     * @param areaId the id of an area to filter the messages on
     * @param categoryId the id of a category to filter the messages on
     * @return the active MSI messages filtered according to the parameters
     */
    public Response getActiveMessages(
            Request request,
            boolean refresh,
            String format,
            String lang,
            boolean details,
            String types,
            Integer areaId,
            Integer categoryId
    ) {

        try {
            // Check if we need to refresh the data
            if (refresh) {
                getProviderService().loadMessages();
            }

            // Fetch the filtered set of data
            MessageFilter filter = new MessageFilter()
                    .lang(lang)
                    .detailed(details)
                    .area(areaId)
                    .category(categoryId)
                    .types(types == null ? null : types.split(","));

            List<Message> messages = getProviderService().getCachedMessages(filter);

            // Compute expiry 10 min from now
            Date expirationDate = new Date(System.currentTimeMillis() + 10L * 60L * 1000L);

            // Compute the content type
            format = "xml".equalsIgnoreCase(format) ? "xml" : "json";
            String contentType = String.format("application/%s;charset=UTF-8", format);

            // Check for an ETag match
            EntityTag etag = new EntityTag(getProviderService().getETagToken(format, filter, messages), true);
            Response.ResponseBuilder responseBuilder = request.evaluatePreconditions(etag);
            if (responseBuilder != null) {
                // ETag match
                getLog().info("Message list unchanged. Return code 304");
                return responseBuilder
                        .expires(expirationDate)
                        .build();
            }

            getLog().info("Returning message list");
            return Response
                    .ok(new GenericEntity<List<Message>>(messages) {}, contentType)
                    .expires(expirationDate)
                    .tag(etag)
                    .build();


        } catch (Exception e) {
            getLog().error("Failed loading active messages: " + e.getMessage());
            throw new WebApplicationException("Error loading messages: " + e.getMessage(), 500);
        }
    }

}
