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

import dk.dma.msiproxy.common.repo.RepositoryService;
import dk.dma.msiproxy.model.MessageFilter;
import dk.dma.msiproxy.model.msi.Category;
import dk.dma.msiproxy.model.msi.Message;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.infinispan.Cache;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An abstract base class for MSI providers.
 */
public abstract class AbstractProviderService {

    public static String MESSAGE_REPO_ROOT_FOLDER = "messages";

    public static final Pattern MESSAGE_ATTACHMENT_FILE_PATTERN = Pattern.compile("^/?messages/\\w+/\\w+/\\w+/(?<id>\\d+)/(?<file>.+)$");
    public static final Pattern MESSAGE_REPO_FILE_PATTERN = Pattern.compile("^/?rest/repo/file/messages/\\w+/\\w+/\\w+/(?<id>\\d+)/(?<file>.+)$");

    protected Logger log = LoggerFactory.getLogger(AbstractProviderService.class);
    protected List<Message> messages = new CopyOnWriteArrayList<>();
    protected long fetchTime = -1L;

    /**
     * Returns a unique id for the implementing provider service
     * @return a unique id for the implementing provider service
     */
    public abstract String getProviderId();

    /**
     * Returns a priority for this provider
     * @return a priority for this provider
     */
    public abstract int getPriority();

    /**
     * Returns the list of supported languages codes for this provider.
     * The languages should be returned in a prioritized order
     * @return the list of supported languages codes for this provider
     */
    public abstract String[] getLanguages();

    /**
     * Returns the language if it is supported by this provider.
     * Otherwise, returns the default language.
     * @param lang the language to test
     * @return a supported language
     */
    public String getLanguage(String lang) {
        return Arrays.asList(getLanguages()).stream()
                .filter(l -> l.equalsIgnoreCase(lang))
                .findFirst()
                .orElse(getLanguages()[0]);
    }

    /**
     * Returns a reference to the message cache service
     * @return a reference to the message cache service
     */
    public abstract MessageCache getMessageCache();

    /**
     * Returns a reference to the repository service
     * @return a reference to the repository service
     */
    public abstract RepositoryService getRepositoryService();

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
     * Returns the message with the given ID
     * @param messageId the message ID
     * @return the message, or null if not found
     */
    public Message getMessage(Integer messageId) {
        return getActiveMessages().stream()
                .filter(msg -> msg.getId().equals(messageId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Updates the full list of active MSI messages
     * @param messages the new full list of active MSI messages
     */
    protected synchronized void setActiveMessages(List<Message> messages) {
        this.messages = new CopyOnWriteArrayList<>(messages);
        this.fetchTime = System.currentTimeMillis();

        // Enforce the provider attribute of the messages
        this.messages.forEach(msg -> msg.setProvider(getProviderId()));

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

    /***************************************/
    /** Repo methods                      **/
    /***************************************/

    /**
     * Returns the repository folder for the given message
     * @param id the id of the message
     * @return the associated repository folder
     */
    public Path getMessageRepoFolder(Integer id) throws IOException {
        String repoFolder = MESSAGE_REPO_ROOT_FOLDER + "/" + getProviderId();
        return  getRepositoryService().getHashedSubfolder(repoFolder, String.valueOf(id), true, false);
    }

    /**
     * Returns the repository file for the given message file
     * @param id the id of the message
     * @param name the file name
     * @return the associated repository file
     */
    public Path getMessageFileRepoPath(Integer id, String name) throws IOException {
        return  getMessageRepoFolder(id).resolve(name);
    }

    /**
     * Returns the repository URI for the message folder
     * @param id the id of the message
     * @return the associated repository URI
     */
    public String getMessageFolderRepoPath(Integer id) throws IOException {
        return getRepositoryService().getRepoPath(getMessageRepoFolder(id));
    }

    /**
     * Returns the repository URI for the given message file
     * @param id the id of the message
     * @param name the file name
     * @return the associated repository URI
     */
    public String getMessageFileRepoUri(Integer id, String name) throws IOException {
        Path file = getMessageRepoFolder(id).resolve(name);
        return getRepositoryService().getRepoUri(file);
    }

    /***************************************/
    /** Repo clean-up methods             **/
    /***************************************/

    /**
     * May be called periodically to clean up the message repo folder associated
     * with the provider.
     * <p>
     * The procedure will determine which repository message ID's are still active.
     * and delete folders associated with messages ID's that are not active anymore.
     */
    public void cleanUpMessageRepoFolder() {

        long t0 = System.currentTimeMillis();

        // Compute the ID's for message repository folders to keep
        Set<Integer> ids = computeReferencedMessageIds(messages);

        // Build a lookup map of all the paths that ara still active
        Set<Path> paths = new HashSet<>();
        ids.forEach(id -> {
            try {
                Path path = getMessageRepoFolder(id);
                // Add the path and the hashed sub-folders above it
                paths.add(path);
                paths.add(path.getParent());
                paths.add(path.getParent().getParent());
            } catch (IOException e) {
                log.error("Failed computing " + getProviderId() + "  message repo paths for id " + id + ": " + e.getMessage());
            }
        });

        // Scan all sub-folders and delete those
        Path messageRepoRoot = getRepositoryService().getRepoRoot()
                .resolve(MESSAGE_REPO_ROOT_FOLDER)
                .resolve(getProviderId());
        paths.add(messageRepoRoot);

        try {
            Files.walkFileTree(messageRepoRoot, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!paths.contains(dir)) {
                        log.info("Deleting message repo directory :" + dir);
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!paths.contains(file.getParent())) {
                        log.info("Deleting message repo file      :" + file);
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Failed cleaning up " + getProviderId() + " message repo: " + e.getMessage());
        }

        log.info(String.format("Cleaned up %s message repo in %d ms",
                getProviderId(),
                System.currentTimeMillis() - t0));
    }

    /**
     * The procedure will determine which repository message ID's are still active.
     * <p>
     * In addition to the actual ID's of active message, look at the attachments and
     * referenced files in message HTML description fields, since these may reference
     * attachments for non-active messages.
     *
     * @param messages the list of active messages
     * @return the ID's for message repository folders to keep
     */
    private Set<Integer> computeReferencedMessageIds(List<Message> messages) {
        Set<Integer> ids = new HashSet<>();

        // First, add the ID of the message
        messages.forEach(msg -> ids.add(msg.getId()));

        // Add all message ID's referenced by message attachments
        messages.stream()
                .filter(msg -> msg.getAttachments() != null && msg.getAttachments().size() > 0)
                .flatMap(msg -> msg.getAttachments().stream())
                .forEach(att -> {
                    Matcher m = MESSAGE_ATTACHMENT_FILE_PATTERN.matcher(att.getPath());
                    if (m.matches()) {
                        ids.add(Integer.valueOf(m.group("id")));
                    }
                });

        // Add all message ID's referenced by message HTML description fields
        messages.stream()
                .filter(msg -> msg.getDescs() != null && msg.getDescs().size() > 0)
                .flatMap(msg -> msg.getDescs().stream())
                .filter(desc -> StringUtils.isNotBlank(desc.getDescription()))
                .forEach(desc -> {
                    try {
                        // Process files referenced by <a> "href" attributes and <img> "src" attributes
                        Document doc = Jsoup.parse(desc.getDescription());
                        computeReferencedMessageIds(ids, doc, "a", "href");
                        computeReferencedMessageIds(ids, doc, "img", "src");
                    } catch (Exception ex) {
                        log.trace("Failed computing referenced messages " + ex.getMessage());
                    }
                });

        return ids;
    }


    /**
     * If the given element attribute references a message repo folder, add the message ID to the ids list.
     * @param ids the message ID list
     * @param doc the HTML document
     * @param tag the HTML tag to process
     * @param attr the attribute of the HTML tag to process
     */
    private void computeReferencedMessageIds(Set<Integer> ids, Document doc, String tag, String attr) {
        doc.select(String.format("%s[%s]", tag, attr)).stream()
                .filter(e -> e.attr(tag) != null)
                .forEach(e -> {
                    Matcher m = MESSAGE_REPO_FILE_PATTERN.matcher(e.attr(attr));
                    if (m.matches()) {
                        ids.add(Integer.valueOf(m.group("id")));
                    }
                });
    }

}
