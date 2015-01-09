package dk.dma.msiproxy.common.service;

import dk.dma.msiproxy.model.MessageFilter;
import dk.dma.msiproxy.model.msi.Category;
import dk.dma.msiproxy.model.msi.Message;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.infinispan.Cache;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * An abstract base class for MSI providers.
 */
public abstract class AbstractProviderService {

    protected List<Message> messages = new CopyOnWriteArrayList<>();
    protected long fetchTime = -1L;

    /**
     * Returns a unique id for the implementing provider service
     * @return a unique id for the implementing provider service
     */
    public abstract String getProviderId();

    /**
     * Returns a reference to the message cache service
     * @return a reference to the message cache service
     */
    public abstract MessageCache getMessageCache();

    /**
     * Returns a reference to the cache
     * @return a reference to the cache
     */
    public Cache<String, List<Message>> getCache() {
        return getMessageCache().getCache(getProviderId());
    }


    /**
     * Returns the full list of active legacy MSI messages
     * @return the full list of active legacy MSI messages
     */
    public synchronized List<Message> getActiveMessages() {
        return messages;
    }

    /**
     * Updates the full list of active MSI messages
     * @param messages the new full list of active MSI messages
     */
    protected synchronized void setActiveMessages(List<Message> messages) {
        this.messages = new CopyOnWriteArrayList<>(messages);
        this.fetchTime = System.currentTimeMillis();
        getCache().clear();
    }

    /**
     * Returns the key to use for caching messages defined by the given filter
     * @param filter the message filter
     * @return the key to use for caching messages defined by the given filter
     */
    public String getCacheKey(MessageFilter filter) {
        return String.format(
                "%s_%d_%s",
                getProviderId(),
                fetchTime,
                filter.getKey()
        );
    }

    /**
     * Computes an ETag token for the given message list
     * @param format the format to return the messages in
     * @param filter the message filter
     * @param messages the list of messages to compute an ETag token for
     * @return an ETag token for the given message list
     */
    public String getETagToken(String format, MessageFilter filter, List<Message> messages) {
        return DigestUtils.md5Hex(
                String.format(
                    "%s_%s_%s",
                    StringUtils.defaultString(format),
                    messages.stream()
                            .map(msg -> msg.getId().toString() + msg.getUpdated().getTime())
                            .collect(Collectors.joining()),
                    getCacheKey(filter)
                )
        );
    }

    /**
     * Returns a filtered view of the message list
     * @param filter the data filter
     * @return the messages
     */
    public List<Message> getCachedMessages(MessageFilter filter) {
        if (filter == null || filter.isEmpty()) {
            return messages;
        }

        String cacheKey = getCacheKey(filter);
        List<Message> result = getCache().get(cacheKey);
        if (result == null) {
            result = filter.filter(messages);
            getCache().put(cacheKey, result);
        }
        return result;
    }

    /**
     * Implemented by subclasses. Loads the messages from the data source
     * @return the resulting list of messages
     */
    public abstract List<Message> loadMessages();

    /**
     * Returns a default firing exercise category
     * @return a default firing exercise category
     */
    public Category getDefaultFiringExerciseCategory() {
        Category category = new Category();
        category.setId(-1000);
        category.createDesc("en").setName("Firing Exercises");
        category.createDesc("da").setName("Skydeøvelser");
        return category;
    }

}
