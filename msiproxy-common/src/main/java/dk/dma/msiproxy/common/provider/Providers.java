package dk.dma.msiproxy.common.provider;

import dk.dma.msiproxy.common.util.CdiHelper;
import dk.dma.msiproxy.model.msi.Message;
import org.slf4j.Logger;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A service that maintains a list of all registered MSI providers
 */
@Singleton
@Startup
@Lock(LockType.READ)
public class Providers {

    @Inject
    Logger log;

    /**
     * Contains the list of registered providers mapped by the provider id
     */
    Map<String, Class<? extends AbstractProviderService>> providers = new HashMap<>();

    /**
     * Should be called from the @PostConstruct method of a provider service
     *
     * @param providerService the provider service to register
     */
    public void registerProvider(AbstractProviderService providerService) {
        Objects.requireNonNull(providerService);
        providers.put(providerService.getProviderId(), providerService.getClass());
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
     * Returns the provider service bean for the given provider ID or null if not found
     * @param providerId the provider ID
     * @return the instantiated provider service or null
     */
    public AbstractProviderService getProvider(String providerId) throws NamingException {
        try {
            return instantiateProvider(providerId);
        } catch (NamingException e) {
            log.warn("Error instantiating provider " + providerId);
        }
        return null;
    }

    /**
     * Instantiates the provider service bean for the given provider ID
     * @param providerId the provider ID
     * @return the instantiated provider service
     */
    private AbstractProviderService instantiateProvider(String providerId) throws NamingException {
        return CdiHelper.getBean(providers.get(providerId));
    }


}
