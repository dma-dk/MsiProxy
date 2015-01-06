package dk.dma.msiproxy.dkprovider.service;

import dk.dma.msiproxy.dkprovider.conf.DkMsiDB;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.sql.SQLException;

/**
 * Created by peder on 06/01/2015.
 */
@Singleton
@Startup
@Path("/dk/messages")
public class DkProviderRestService {

    @Inject
    @DkMsiDB
    EntityManager em;

    /**
     * Returns the legacy import status
     */
    @GET
    @Path("/test")
    @Produces("application/json")
    public String getMessageCount() throws SQLException {

        return em.createNativeQuery("select count(*) from message").getSingleResult().toString();

        /**
        StringBuilder result = new StringBuilder();
        Session session = em.unwrap(Session.class);
        session.doWork(connection -> {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select count(*) from message");
            rs.next();
            result.append("ROWS " + rs.getInt(1));
        });
        return result.toString();
         **/
    }

}
