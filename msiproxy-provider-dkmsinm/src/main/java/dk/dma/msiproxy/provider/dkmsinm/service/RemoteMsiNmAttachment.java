package dk.dma.msiproxy.provider.dkmsinm.service;

import dk.dma.msiproxy.common.provider.AbstractProviderService;
import dk.dma.msiproxy.common.repo.RemoteAttachment;
import dk.dma.msiproxy.common.util.WebUtils;
import dk.dma.msiproxy.model.msi.Attachment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used for copying remote attachments and referenced files from an MSI-NM server to the local repository.
 */
public class RemoteMsiNmAttachment implements RemoteAttachment {

    public static final Pattern MSINM_REPO_FILE_PATTERN = Pattern.compile("^/?rest/repo/file/messages/\\d+/\\d+/(?<id>\\d+)/(?<file>.+)$");
    public static final Pattern MSINM_ATTACHMENT_FILE_PATTERN = Pattern.compile("^/?messages/\\d+/\\d+/(?<id>\\d+)/(?<file>.+)$");
    public static final String MSINM_REPO_FILE_PATH = "/rest/repo/file/";

    Path localFileRepoPath;
    String localFileRepoUri;
    String remoteFileUrl;
    boolean copyLocal;

    /**
     * Creates a remote attachment from an existing attachment as received from the MSI-NM server
     *
     * @param providerService the provider service
     * @param remoteServerUrl the remote server url
     * @param att the attachment to convert
     * @return the remote attachment
     */
    public static RemoteMsiNmAttachment fromAttachment(
            AbstractProviderService providerService,
            String remoteServerUrl,
            Attachment att) throws IOException {

        RemoteMsiNmAttachment ratt = new RemoteMsiNmAttachment();

        // A message attachment will be placed in a message repo folder on the MSI-NM server.
        // Example: "messages/1/19/6456/tycho-brahe-stjerneborg.jpg"
        // This is the file "tycho-brahe-stjerneborg.jpg" in the folder of the message
        // with the ID "6456" (note, "1/19" is a sub-folder hash of "6456").
        Matcher m = MSINM_ATTACHMENT_FILE_PATTERN.matcher(att.getPath());
        if (m.matches()) {
            Integer messageId = Integer.valueOf(m.group("id"));
            String file = m.group("file");

            // Compute the target local repo attachment path, URI and the full URL of the remote attachment
            ratt.localFileRepoPath = providerService.getMessageFileRepoPath(messageId, att.getName());
            ratt.localFileRepoUri = concat(providerService.getMessageFolderRepoPath(messageId), att.getName());
            ratt.remoteFileUrl = concat(remoteServerUrl, MSINM_REPO_FILE_PATH, att.getPath());

            // Only copy the file to the local repo if it is not already there of if it has a modification data older than
            // stated by the attachment.
            ratt.copyLocal = !Files.exists(ratt.localFileRepoPath) ||
                    (att.getUpdated() != null && Files.getLastModifiedTime(ratt.localFileRepoPath).toMillis() < att.getUpdated().getTime());

        } else {
            // This case should never really happen...

            // Check if the attachment path is a full URL
            if (att.getPath().toLowerCase().startsWith("http")) {
                ratt.remoteFileUrl = att.getPath();
            } else {
                // If the remote attachment path is relative, assemble a global url
                ratt.remoteFileUrl = concat(remoteServerUrl, MSINM_REPO_FILE_PATH, att.getPath());
            }
        }
        return ratt;
    }

    /**
     * Creates a remote attachment from a referenced file in the message HTML description field.
     *
     * @param providerService the provider service
     * @param remoteServerUrl the remote server url
     * @param path the referenced file path
     * @return the remote attachment
     */
    public static RemoteMsiNmAttachment fromReferencedFile(
            AbstractProviderService providerService,
            String remoteServerUrl,
            String path) throws IOException {

        RemoteMsiNmAttachment ratt = new RemoteMsiNmAttachment();

        // The path will be the "src" attribute of an <img> tag, or a "href" attribute
        // of an <a> tag. First, check if it is pointing to an attachment in a
        // message repo folder.
        // Example: "/rest/repo/file/messages/1/19/6456/tycho-brahe-stjerneborg.jpg"
        Matcher m = MSINM_REPO_FILE_PATTERN.matcher(path);
        if (m.matches()) {
            Integer messageId = Integer.valueOf(m.group("id"));
            // NB: The file path will have been URL encoded.
            String file = WebUtils.decode(m.group("file"));

            // Compute the target local repo attachment path, URI and the full URL of the remote attachment
            ratt.localFileRepoPath = providerService.getMessageFileRepoPath(messageId, file);
            ratt.localFileRepoUri = providerService.getMessageFileRepoUri(messageId, file);
            ratt.remoteFileUrl = concat(remoteServerUrl, path);

            // We only want to copy the attachment locally if it does not already exist
            ratt.copyLocal = !Files.exists(ratt.localFileRepoPath);

        } else {
            // Check if the attachment path is a full URL
            if (path.toLowerCase().startsWith("http")) {
                ratt.remoteFileUrl = path;
            } else {
                // If the remote attachment path is relative, assemble a global url
                ratt.remoteFileUrl = concat(remoteServerUrl, path);
            }
        }
        return ratt;
    }

    /**
     * Concatenates a list of web parts using a "/" character
     * @param parts the web parts
     * @return the concatenated parts
     */
    private static String concat(String... parts) {
        StringBuilder str = new StringBuilder();
        for (String part : parts) {
            if (!part.startsWith("/") && str.length() > 0 && !str.toString().endsWith("/")) {
                str.append("/");
            }
            str.append(part);
        }
        return str.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getLocalFileRepoPath() {
        return localFileRepoPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocalFileRepoUri() {
        return localFileRepoUri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRemoteFileUrl() {
        return remoteFileUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCopyLocal() {
        return copyLocal;
    }
}
