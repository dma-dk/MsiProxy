package dk.dma.msiproxy.dkprovider.service;

import dk.dma.msiproxy.common.util.JsonUtils;
import dk.dma.msiproxy.dkprovider.conf.DkMsiDB;
import dk.dma.msiproxy.model.msi.Message;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a business interface for accessing Danish legacy MSI messages
 */
@Stateless
public class DkProviderService {

    @Inject
    @DkMsiDB
    EntityManager em;

    /**
     * Returns the list of active legacy MSI messages
     * @return the list of active legacy MSI messages
     */
    public List<Message> getActiveMessages() {

        List<Message> messages = new ArrayList<>();

        try {
            URL url = new URL("https://msinm-test.e-navigation.net/rest/messages/published?lang=en&sortBy=AREA&sortOrder=ASC");
            try (InputStream is = url.openStream()) {
                MessageSearchResult result = JsonUtils.fromJson(is, MessageSearchResult.class);
                messages = result.getMessages();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return messages;
    }
}
