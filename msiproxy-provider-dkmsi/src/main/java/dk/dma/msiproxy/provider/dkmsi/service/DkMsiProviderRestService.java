package dk.dma.msiproxy.provider.dkmsi.service;

import dk.dma.msiproxy.common.provider.AbstractProviderRestService;
import dk.dma.msiproxy.common.provider.AbstractProviderService;
import org.jboss.resteasy.annotations.GZIP;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

/**
 * Provides a REST interface for accessing Danish legacy MSI messages
 */
@Path("/" + DkMsiProviderService.PROVIDER_ID + "/v1/service")
public class DkMsiProviderRestService extends AbstractProviderRestService {

    @Inject
    Logger log;

    @Inject
    DkMsiProviderService providerService;

    @Context
    Request request;

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractProviderService getProviderService() {
        return providerService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Logger getLog() {
        return log;
    }


    /**
     * Returns the active MSI messages in the requested format and language.
     *
     * @param format either "json" (default) or "xml"
     * @param refresh whether to force a refresh of the data or not
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
            @QueryParam("refresh") @DefaultValue("false") boolean refresh,
            @QueryParam("format") @DefaultValue("json") String format,
            @QueryParam("lang") @DefaultValue("da") String lang,
            @QueryParam("details") @DefaultValue("true") boolean details,
            @QueryParam("types") String types,
            @QueryParam("areaId") Integer areaId,
            @QueryParam("categoryId") Integer categoryId
    ) {
        return super.getActiveMessages(
                request,
                refresh,
                format,
                lang,
                details,
                types,
                areaId,
                categoryId);
    }

}
