package dk.dma.msiproxy.dkprovider.service;

import dk.dma.msiproxy.common.service.AbstractProviderService;
import dk.dma.msiproxy.common.service.MessageCache;
import dk.dma.msiproxy.common.util.JsonUtils;
import dk.dma.msiproxy.dkprovider.conf.DkMsiDB;
import dk.dma.msiproxy.model.msi.Message;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Provides a business interface for accessing Danish legacy MSI messages
 */
@Singleton
@Lock(LockType.READ)
@Startup
public class DkProviderService extends AbstractProviderService {

    public static final String PROVIDER_ID = "dk_msi";

    @Inject
    Logger log;

    @Inject
    MessageCache messageCache;

    @Inject
    @DkMsiDB
    EntityManager em;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageCache getMessageCache() {
        return messageCache;
    }

    /**
     * Called at start up.
     */
    @PostConstruct
    public void init() {
        // Load messages
        loadMessages();
    }

    /**
     * Called every 5 minutes to update message list
     */
    @Schedule(persistent=false, second="38", minute="*/5", hour="*", dayOfWeek="*", year="*")
    protected void loadMessagesPeriodically() {
        loadMessages();
    }

    /**
     * Loads the MSI messages asynchronously
     * @return the resulting list of messages
     */
    @Asynchronous
    public Future<List<Message>> loadMessages() {

        long t0 = System.currentTimeMillis();
        try {
            String url ="https://msinm-test.e-navigation.net/rest/messages/published?sortBy=AREA&sortOrder=ASC";
            URLConnection con = new URL(url).openConnection();
            con.setConnectTimeout(5000); //  5 seconds
            con.setReadTimeout(10000);   // 10 seconds

            try (InputStream is = con.getInputStream()) {
                MessageSearchResult searchResult = JsonUtils.fromJson(is, MessageSearchResult.class);
                setActiveMessages(searchResult.getMessages());
                log.info(String.format("Loaded %d messages in %s ms", messages.size(), System.currentTimeMillis() - t0));
            }

        } catch (Exception e) {
            log.error("Failed loading messages: " + e.getMessage());
        }

        return new AsyncResult<>(messages);
    }
}
