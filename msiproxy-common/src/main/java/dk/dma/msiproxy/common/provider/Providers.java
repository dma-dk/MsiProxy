/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.msiproxy.common.provider;

import dk.dma.msiproxy.common.util.CdiHelper;
import org.slf4j.Logger;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
     * Returns the provider service bean for the given provider ID or null if not found.
     * @param providerId the provider ID
     * @return the instantiated provider service or null
     */
    public AbstractProviderService getProvider(String providerId) {
        try {
            return instantiateProvider(providerId);
        } catch (Exception e) {
            log.warn("Error instantiating provider " + providerId);
        }
        return null;
    }

    /**
     * Returns the provider service beans for the given colon-separated provider ID's.
     * If an invalid provider ID is included, the provider service is excluded from the result.
     * <p>
     * The special "all" provider ID means that all providers are used
     *
     * @param providerIds the colon-separated provider ID's
     * @return the instantiated provider service beans
     */
    public List<AbstractProviderService> getProviders(String providerIds) {

        if ("all".equalsIgnoreCase(providerIds)) {
            return getProviders(providers.keySet().stream().collect(Collectors.joining(":")));
        } else if (providerIds == null) {
            return Arrays.asList(getProvider(null));
        }

        return Arrays.asList(providerIds.split(":"))
                .stream()
                .map(this::getProvider)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Instantiates the provider service bean for the given provider ID.
     * If null is specified as the provider ID, the provider with the highest priority is used
     * @param providerId the provider ID
     * @return the instantiated provider service
     */
    private AbstractProviderService instantiateProvider(String providerId) throws Exception {
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

