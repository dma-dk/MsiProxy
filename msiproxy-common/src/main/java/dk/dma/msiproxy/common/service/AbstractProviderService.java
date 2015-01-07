package dk.dma.msiproxy.common.service;

import dk.dma.msiproxy.model.DataFilter;
import dk.dma.msiproxy.model.msi.Message;
import org.infinispan.Cache;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * An abstract base class for MSI providers.
 * <p>
 *     The implementing class should be annotated with {@code @Singleton}.
 *     Furthermore, it should call {@code init()} in a {@code @PostConstruct} method
 *     and {@code destroy()} in a {@code @PreDestroy} method.
 * </p>
 */
public abstract class AbstractProviderService {

    protected List<Message> messages = new CopyOnWriteArrayList<>();

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
     * Returns the list of active legacy MSI messages
     * @return the list of active legacy MSI messages
     */
    public List<Message> getActiveMessages() {
        return messages;
    }

    /**
     * Returns a filtered view of the message list
     * @param dataFilter the data filter
     * @return the messages
     */
    public List<Message> getCachedMessages(DataFilter dataFilter) {
        if (dataFilter == null) {
            return messages;
        }

        String cacheKey = dataFilter.toString();
        List<Message> result = getCache().get(cacheKey);
        if (result == null) {
            result = new CopyOnWriteArrayList<>();
            result.addAll(messages.stream()
                    .map(msg -> new Message(msg, dataFilter))
                    .collect(Collectors.toList()));
            getCache().put(cacheKey, result);
        }
        return result;
    }
}
