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
package dk.dma.msiproxy.common.publish;

import dk.dma.msiproxy.common.conf.MsiProxyDB;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Business interface to manage Published entities.
 */
@Singleton
@Startup
public class PublisherService {

    @Inject
    @MsiProxyDB
    EntityManager em;

    @PostConstruct
    public void testingDBConnection() {
        List<Integer> ids = new ArrayList<>();
        ids.add(1);

        System.out.println("********************** "
                + getPublishedResult(Publisher.TWITTER, ids));
    }

    /**
     * Records that the message has been published to the given publisher
     * @param publisher the publisher
     * @param messageId the message id
     */
    public void flagPublished(Publisher publisher, Integer messageId) {
        Objects.requireNonNull(publisher);
        Objects.requireNonNull(messageId);

        Published p = new Published();
        p.setPublisher(publisher);
        p.setMessageId(messageId);
        em.persist(p);
    }

    /**
     * Returns the list of message ids that have already been published
     * @param publisher the publisher type
     * @param messageIds the messages to check
     * @return the ids of the messages that have already been published
     */
    public List<Integer> getPublishedResult(Publisher publisher, List<Integer> messageIds) {

        return em.createNamedQuery("Published.selectPublished", Published.class)
                .setParameter("publisher", publisher)
                .setParameter("messageIds", messageIds)
                .getResultList()
                .stream()
                .map(Published::getMessageId)
                .collect(Collectors.toList());
    }
}
