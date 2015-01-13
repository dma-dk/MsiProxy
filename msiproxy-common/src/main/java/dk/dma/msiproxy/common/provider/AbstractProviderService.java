package dk.dma.msiproxy.common.provider;

import dk.dma.msiproxy.common.repo.RepositoryService;
import dk.dma.msiproxy.model.MessageFilter;
import dk.dma.msiproxy.model.msi.Category;
import dk.dma.msiproxy.model.msi.Message;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.infinispan.Cache;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * An abstract base class for MSI providers.
 */
public abstract class AbstractProviderService {

    public static String MESSAGE_REPO_ROOT_FOLDER = "messages";

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
        return  getRepositoryService().getHashedSubfolder(repoFolder, String.valueOf(id), true);
    }

    /**
     * Returns the repository folder for the given message
     * @param message the message
     * @return the associated repository folder
     */
    public Path getMessageRepoFolder(Message message) throws IOException {
        return  getMessageRepoFolder(message.getId());
    }

    /**
     * Returns the repository file for the given message file
     * @param message the message
     * @param name the file name
     * @return the associated repository file
     */
    public Path getMessageFileRepoPath(Message message, String name) throws IOException {
        return  getMessageRepoFolder(message).resolve(name);
    }

    /**
     * Returns the repository URI for the message folder
     * @param message the message
     * @return the associated repository URI
     */
    public String getMessageFolderRepoPath(Message message) throws IOException {
        return getRepositoryService().getRepoPath(getMessageRepoFolder(message));
    }

    /**
     * Returns the repository URI for the given message file
     * @param message the message
     * @param name the file name
     * @return the associated repository URI
     */
    public String getMessageFileRepoUri(Message message, String name) throws IOException {
        Path file = getMessageRepoFolder(message).resolve(name);
        return getRepositoryService().getRepoUri(file);
    }

}
