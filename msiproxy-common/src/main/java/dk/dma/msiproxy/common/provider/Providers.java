package dk.dma.msiproxy.common.provider;

import dk.dma.msiproxy.common.util.CdiHelper;
import dk.dma.msiproxy.model.MessageFilter;
import dk.dma.msiproxy.model.msi.Message;
import org.slf4j.Logger;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A service that maintains a list of all registered MSI providers
 */
@Named("providers")
@Singleton
@Startup
@Lock(LockType.READ)
public class Providers {

    @Inject
    Logger log;

    /**
     * Contains the list of registered providers mapped by the provider id
     */
    Map<String, ProviderContext> providers = new ConcurrentHashMap<>();

    /**
     * Should be called from the @PostConstruct method of a provider service
     *
     * @param providerService the provider service to register
     */
    public void registerProvider(AbstractProviderService providerService) {
        Objects.requireNonNull(providerService);
        providers.put(providerService.getProviderId(),
                new ProviderContext(providerService.getPriority(), providerService.getClass()));
        log.info("Registered MSI provider " + providerService.getProviderId());
    }

    /**
     * Returns the message with the given ID from the given provider
     *
     * @param providerId the provider ID
     * @param messageId  the message ID
     * @return the message, or null if not found
     */
    public Message getMessage(String providerId, Integer messageId) {
        try {
            return instantiateProvider(providerId).getMessage(messageId);
        } catch (NamingException e) {
            log.warn("Error instantiating provider " + providerId);
        }
        return null;
    }

    /**
     * Returns the list of active messages from the given provider
     *
     * @param providerId the provider ID
     * @return the messages, or null if not found
     */
    public List<Message> getActiveMessages(String providerId) {
        try {
            return instantiateProvider(providerId).getActiveMessages();
        } catch (NamingException e) {
            log.warn("Error instantiating provider " + providerId);
        }
        return null;
    }

    /**
     * Returns a filtered view of the message list from the given provider
     *
     * @param providerId the provider ID
     * @param filter the data filter
     * @return the messages
     */
    public List<Message> getCachedMessages(String providerId, MessageFilter filter) {
        try {
            return instantiateProvider(providerId).getCachedMessages(filter);
        } catch (NamingException e) {
            log.warn("Error instantiating provider " + providerId);
        }
        return null;
    }

    /**
     * Returns the provider service bean for the given provider ID or null if not found.
     * @param providerId the provider ID
     * @return the instantiated provider service or null
     */
    public AbstractProviderService getProvider(String providerId) {
        try {
            return instantiateProvider(providerId);
        } catch (NamingException e) {
            log.warn("Error instantiating provider " + providerId);
        }
        return null;
    }

    /**
     * Instantiates the provider service bean for the given provider ID.
     * If null is specified as the provider ID, the provider with the highest priority is used
     * @param providerId the provider ID
     * @return the instantiated provider service
     */
    private AbstractProviderService instantiateProvider(String providerId) throws NamingException {
        Class<? extends AbstractProviderService> clazz;
        if (providerId == null) {
            clazz = providers.entrySet().stream()
                    .map(Map.Entry::getValue)
                    .max(Comparator.comparingInt(ProviderContext::getPriority))
                    .get()
                    .getProviderClass();
        } else {
            clazz = providers.get(providerId).getProviderClass();
        }
        return CdiHelper.getBean(clazz);
    }

    /**
     * Used for associating a provider class with the priority of the provider
     */
    public static class ProviderContext {
        int priority;
        Class<? extends AbstractProviderService> providerClass;

        public ProviderContext(int priority, Class<? extends AbstractProviderService> providerClass) {
            this.priority = priority;
            this.providerClass = providerClass;
        }

        public int getPriority() {
            return priority;
        }

        public Class<? extends AbstractProviderService> getProviderClass() {
            return providerClass;
        }
    }
}

