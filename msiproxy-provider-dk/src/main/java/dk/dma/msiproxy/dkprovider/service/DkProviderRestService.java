package dk.dma.msiproxy.dkprovider.service;

import dk.dma.msiproxy.model.msi.Message;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

/**
 * Provides a REST interface for accessing Danish legacy MSI messages
 */
@Singleton
@Startup
@Path("/dk/messages")
public class DkProviderRestService {

    @Inject
    DkProviderService providerService;

    /**
     * Returns the legacy import status
     */
    @GET
    @Path("/active")
    @Produces({"application/json;charset=UTF-8", "application/xml;charset=UTF-8" })
    @GZIP
    @NoCache
    public List<Message> getActiveMessages() {
        return providerService.getActiveMessages();
    }

}
