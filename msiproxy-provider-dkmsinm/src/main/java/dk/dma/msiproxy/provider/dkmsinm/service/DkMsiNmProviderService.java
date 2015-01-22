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
package dk.dma.msiproxy.provider.dkmsinm.service;

import dk.dma.msiproxy.common.provider.AbstractProviderService;
import dk.dma.msiproxy.common.provider.MessageCache;
import dk.dma.msiproxy.common.provider.Providers;
import dk.dma.msiproxy.common.repo.RemoteAttachment;
import dk.dma.msiproxy.common.repo.RemoteAttachmentLoader;
import dk.dma.msiproxy.common.repo.RepositoryService;
import dk.dma.msiproxy.common.settings.annotation.Setting;
import dk.dma.msiproxy.common.util.JsonUtils;
import dk.dma.msiproxy.model.msi.Attachment;
import dk.dma.msiproxy.model.msi.Message;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @Setting(value = "dkmsinmUrl", defaultValue = "https://msinm-test.e-navigation.net")
    String serverUrl;

    @Inject
    Logger log;

    @Inject
    Providers providers;

    @Inject
    MessageCache messageCache;

    @Inject
    RepositoryService repositoryService;

    @Inject
    RemoteAttachmentLoader remoteAttachmentLoader;

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

    /***************************************/
    /** Life cycle methods                **/
    /***************************************/

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

    /***************************************/
    /** Scheduling methods                **/
    /***************************************/

    /**
     * Called every 5 minutes to update message list
     */
    @Schedule(persistent=false, second="12", minute="*/5", hour="*", dayOfWeek="*", year="*")
    protected void loadMessagesPeriodically() {
        loadMessages();
    }

    /**
     * Called every hour to clean up the message repo folder
     */
    @Schedule(persistent=false, second="30", minute="17", hour="*/1", dayOfWeek="*", year="*")
    protected void cleanUpMessageRepoFolderPeriodically() {
        cleanUpMessageRepoFolder();
    }

    /***************************************/
    /** Message loading                   **/
    /***************************************/

    /**
     * Returns the url for fetching the list of active messages sorted by area
     * @return the list of active messages sorted by area
     */
    private String getActiveMessagesUrl() {
        return serverUrl + "/rest/messages/published?sortBy=AREA&sortOrder=ASC&attachments=true";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> loadMessages() {

        long t0 = System.currentTimeMillis();
        try {
            URLConnection con = new URL(getActiveMessagesUrl()).openConnection();
            con.setConnectTimeout(5000); //  5 seconds
            con.setReadTimeout(10000);   // 10 seconds

            try (InputStream is = con.getInputStream()) {
                MessageSearchResult searchResult = JsonUtils.fromJson(is, MessageSearchResult.class);

                // Check if there are any changes to the current list of messages
                if (isMessageListUnchanged(searchResult.getMessages())) {
                    log.trace("Legacy MSI messages not changed");
                    return messages;
                }

                // Start synchronizing the attachments
                List<Message> messages = searchResult.getMessages();
                messages = loadRemoteMsiNmAttachments(messages);

                setActiveMessages(messages);
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
                if (!isAttachmentsUnchanged(msg.getAttachments(), activeMsg.getAttachments())) {
                    return false;
                }
            }
            // No changes
            return true;
        }

        return false;
    }

    /**
     * Checks if the list of attachments have changed
     * @param attachments1 the first list of attachments
     * @param attachments2 the second list of attachments
     * @return if the list of attachments have changed
     */
    private boolean isAttachmentsUnchanged(List<Attachment> attachments1, List<Attachment> attachments2) {
        if (attachments1 == null && attachments2 == null) {
            return true;
        } else if (attachments1 == null || attachments2 == null || attachments1.size() != attachments2.size()) {
            return false;
        }
        for (int x = 0; x < attachments1.size(); x++) {
            Attachment att1 = attachments1.get(x);
            Attachment att2 = attachments2.get(x);
            if (!Objects.equals(att1.getUpdated(), att2.getUpdated())) {
                return false;
            }
        }
        return true;
    }

    /***********************************************/
    /** Remote attachment handling                **/
    /***********************************************/

    /**
     * Loads the remote MSI-NM attachments to the local repo, if they do not already exist.
     * <p>
     * There are two types of files:
     * <ul>
     *     <li>Attachments: Attachments part of the JSON message model fetched in the REST call for active messages.</li>
     *     <li>Referenced files: The message HTML description field may contain images or links. If the referenced file
     *                           is a message attachment, fetch it.</li>
     * </ul>
     *
     * If the files are copied to the local repo, the process entails re-writing the attachment path
     * and HTML description records to point to the attachment copy in the local repo.
     *
     * @param messages the list of messages to load attachments for
     * @return the updated messages
     */
    public List<Message> loadRemoteMsiNmAttachments(List<Message> messages) {

        long t0 = System.currentTimeMillis();

        // First, process all the attachments fetched from the MSI-NM server
        Map<Path, RemoteAttachment> attachments = new HashMap<>();
        messages.stream()
                .filter(msg -> msg.getAttachments() != null && msg.getAttachments().size() > 0)
                .flatMap(msg -> convertMessageAttachments(msg).stream())
                .filter(ratt -> ratt.isCopyLocal() && !attachments.containsKey(ratt.getLocalFileRepoPath()))
                .forEach(ratt -> attachments.put(ratt.getLocalFileRepoPath(), ratt));

        // Next, process and rewrite referenced files of the HTML description fields
        messages.stream()
                .filter(msg -> msg.getDescs() != null && msg.getDescs().size() > 0)
                .flatMap(msg -> convertReferencedLinks(msg).stream())
                .filter(ratt -> ratt.isCopyLocal() && !attachments.containsKey(ratt.getLocalFileRepoPath()))
                .forEach(ratt -> attachments.put(ratt.getLocalFileRepoPath(), ratt));

        // Start loading the remote attachments to the local repo asynchronously
        remoteAttachmentLoader.loadRemoteAttachments(attachments.values());

        log.info("Synchronized attachments in " + (System.currentTimeMillis() - t0) + " ms");
        return messages;
    }

    /**
     * Process the attachments of a message and returns a list of corresponding remote attachments
     * @param msg the message to process the attachments for
     * @return the list of corresponding remote attachments
     */
    private List<RemoteAttachment> convertMessageAttachments(Message msg) {
        List<RemoteAttachment> attachments = new ArrayList<>();

        msg.getAttachments().forEach(att -> {
            try {
                // Create a remote attachment value object from the attachment
                RemoteAttachment ratt = RemoteMsiNmAttachment.fromAttachment(this, serverUrl, att);

                // Check if we should re-write the attachment path to point to the local repo
                if (ratt.getLocalFileRepoUri() != null) {
                    att.setPath(ratt.getLocalFileRepoUri());
                }

                attachments.add(ratt);

            } catch (IOException e) {
                log.error("Failed processing attachment for message " + msg.getId() + ": " + att.getPath());
            }
        });
        return attachments;
    }

    /**
     * Utility method that will process the HTML description field and convert images and links to
     * point to the local repository.
     * @param msg the message whose HTML description field to process for referenced files
     * @return the processed HTML
     */
    private List<RemoteAttachment> convertReferencedLinks(Message msg) {
        List<RemoteAttachment> attachments = new ArrayList<>();

        msg.getDescs().forEach(desc -> {
            try {
                // Process files referenced by <a> "href" attributes and <img> "src" attributes
                Document doc = Jsoup.parse(desc.getDescription(), serverUrl);
                convertReferencedLinks(attachments, doc, "a", "href");
                convertReferencedLinks(attachments, doc, "img", "src");
                desc.setDescription(doc.toString());
            } catch (Exception ex) {
                log.warn("Failed parsing message description for message " + msg.getId());
            }
        });
        return attachments;
    }

    /**
     * Make sure that links point to the local repository.
     * @param attachments the list of referenced files to update
     * @param doc the HTML document
     * @param tag the HTML tag to process
     * @param attr the attribute of the HTML tag to process
     */
    protected void convertReferencedLinks(List<RemoteAttachment> attachments, Document doc, String tag, String attr) {

        Elements elms = doc.select(tag + "[" + attr + "]");
        for (Element e : elms) {

            try {
                // Create a remote attachment value object from the link
                RemoteAttachment ratt = RemoteMsiNmAttachment.fromReferencedFile(this, serverUrl, e.attr(attr));

                // Check if it should be copied to the local repo
                if (ratt.getLocalFileRepoUri() != null) {
                    // Re-write the link to point to the local repo
                    e.attr(attr, ratt.getLocalFileRepoUri());
                } else {
                    // Re-write the link to point to the absolute URL
                    e.attr(attr, ratt.getRemoteFileUrl());
                }

                attachments.add(ratt);

            } catch (IOException ex) {
                log.error("Failed processing HTML description");
            }
        }
    }
}
