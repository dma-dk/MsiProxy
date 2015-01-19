package dk.dma.msiproxy.provider.dkmsinm.service;

import dk.dma.msiproxy.common.provider.AbstractProviderService;
import dk.dma.msiproxy.common.provider.MessageCache;
import dk.dma.msiproxy.common.provider.Providers;
import dk.dma.msiproxy.common.repo.RepositoryService;
import dk.dma.msiproxy.common.settings.annotation.Setting;
import dk.dma.msiproxy.common.util.JsonUtils;
import dk.dma.msiproxy.model.msi.Message;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;

/**
 * Provides a business interface for accessing Danish MSI-NM messages
 */
@Singleton
@Lock(LockType.READ)
@Startup
public class DkMsiNmProviderService extends AbstractProviderService {

    public static final String PROVIDER_ID = "dkmsinm";
    public static final int PRIORITY = 100;
    public static final String[] LANGUAGES = { "da", "en" };

    @Inject
    @Setting(value = "dkmsinmUrl", defaultValue = "https://msinm-test.e-navigation.net/rest/messages/published?sortBy=AREA&sortOrder=ASC")
    String url;

    @Inject
    Logger log;

    @Inject
    Providers providers;

    @Inject
    MessageCache messageCache;

    @Inject
    RepositoryService repositoryService;

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
     * {@inheritDoc}
     */
    @Override
    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return PRIORITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getLanguages() {
        return LANGUAGES;
    }

    /**
     * Called at start up.
     */
    @PostConstruct
    public void init() {
        // Register with the providers service
        providers.registerProvider(this);

        // Load messages
        loadMessages();
    }

    /**
     * Called every 5 minutes to update message list
     */
    @Schedule(persistent=false, second="12", minute="*/5", hour="*", dayOfWeek="*", year="*")
    protected void loadMessagesPeriodically() {
        loadMessages();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> loadMessages() {

        long t0 = System.currentTimeMillis();
        try {
            URLConnection con = new URL(url).openConnection();
            con.setConnectTimeout(5000); //  5 seconds
            con.setReadTimeout(10000);   // 10 seconds

            try (InputStream is = con.getInputStream()) {
                MessageSearchResult searchResult = JsonUtils.fromJson(is, MessageSearchResult.class);

                // Check if there are any changes to the current list of messages
                if (isMessageListUnchanged(searchResult.getMessages())) {
                    log.trace("Legacy MSI messages not changed");
                    return messages;
                }

                setActiveMessages(searchResult.getMessages());
                log.info(String.format("Loaded %d MSI-NM messages in %s ms", messages.size(), System.currentTimeMillis() - t0));
            }

        } catch (Exception e) {
            log.error("Failed loading MSI-NM messages: " + e.getMessage());
        }

        return messages;
    }

    /**
     * Checks if the currently list of messages is unchanged from the active messages
     *
     * @param activeMessages the active messages to compare the current list of messages to
     * @return if the currently list of messages is unchanged from the active messages
     */
    private boolean isMessageListUnchanged(List<Message> activeMessages) {
        if (activeMessages.size() == messages.size()) {

            // Check that the message ids and change dates of the two lists are identical
            for (int x = 0; x < messages.size(); x++) {
                Message msg = messages.get(x);
                Message activeMsg = activeMessages.get(x);
                if (!Objects.equals(msg.getId(), activeMsg.getId()) ||
                        !Objects.equals(msg.getUpdated(), activeMsg.getUpdated())) {
                    return false;
                }
            }
            // No changes
            return true;
        }

        return false;
    }


}
