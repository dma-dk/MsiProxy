package dk.dma.msiproxy.provider.dkmsi.service;

import dk.dma.msiproxy.common.service.AbstractProviderService;
import dk.dma.msiproxy.common.service.MessageCache;
import dk.dma.msiproxy.model.msi.Message;
import dk.dma.msiproxy.provider.dkmsi.conf.DkMsiDB;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.AsyncResult;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Provides a business interface for accessing Danish legacy MSI messages
 */
@Singleton
@Lock(LockType.READ)
@Startup
public class DkMsiProviderService extends AbstractProviderService {

    public static final String PROVIDER_ID = "dkmsi";

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
    public Future<List<Message>> loadMessages() {

        long t0 = System.currentTimeMillis();
        try {

            // TBD

        } catch (Exception e) {
            log.error("Failed loading messages: " + e.getMessage());
        }

        return new AsyncResult<>(messages);
    }
}
