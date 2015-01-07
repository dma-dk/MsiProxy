package dk.dma.msiproxy.dkprovider.service;

import dk.dma.msiproxy.model.msi.Message;
import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides a REST interface for accessing Danish legacy MSI messages
 */
@Path("/dk/messages")
public class DkProviderRestService {



    @Inject
    Logger log;

    @Inject
    DkProviderService providerService;

    /**
     * Returns the legacy import status
     *
    @GET
    @Path("/active")
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8" })
    @GZIP
    @NoCache
    public List<Message> getActiveMessages() {
        return providerService.getActiveMessages();
    }
    */

    @GET
    @Path("/active")
    @GZIP
    public Response getActiveMessages(
            @QueryParam("format") @DefaultValue("json") String format,
            @Context Request request) {

        try {
            List<Message> messages = providerService.getActiveMessages();

            // Compute expiry 10 min from now
            Date expirationDate = new Date(System.currentTimeMillis() + 10L * 60L * 1000L);

            // Compute the content type
            String contentType = "xml".equalsIgnoreCase(format) ? "application/xml" : "application/json";
            contentType += ";charset=UTF-8";

            // Check for an ETag match
            // The ETag token is composed by concatenating the format and the MD5 hash of all message ID and updated timestamps
            String etagToken = format + "_"
                    + DigestUtils.md5Hex(messages.stream()
                        .map(msg -> msg.getId().toString() + msg.getUpdated().getTime())
                        .collect(Collectors.joining()));
            EntityTag etag = new EntityTag(etagToken, true);
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
            throw new WebApplicationException(404);
        }
    }


    /**
     * Computes an MD5 Hash for the message list based on the ID and updated date of the messages
     * @param messages the messages
     * @return the MD5 hash
     */
    public String getMessageHash(List<Message> messages) {

        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.reset();
            messages.forEach(msg -> {
                m.update(msg.getId().toString().getBytes());
                //m.update(msg.getUpdated().toString().getBytes());
            });
            byte[] digest = m.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digest.length; ++i) {
                sb.append(Integer.toHexString((digest[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // This should never happen!
            return null;
        }
    }
}
