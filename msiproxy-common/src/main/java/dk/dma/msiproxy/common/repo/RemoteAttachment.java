package dk.dma.msiproxy.common.repo;

import java.nio.file.Path;

/**
 * Used for copying remote attachments and referenced files to the local repository
 */
public interface RemoteAttachment {

    /**
     * Returns the computed file path to the attachment in the local repo
     * @return the computed file path to the attachment in the local repo
     */
    public Path getLocalFileRepoPath();

    /**
     * Returns computed the url-encoded URI to the attachment in the local repo
     * @return computed the url-encoded URI to the attachment in the local repo
     */
    public String getLocalFileRepoUri();

    /**
     * Returns the full URL to the original attachment on the remote server
     * @return the full URL to the original attachment on the remote server
     */
    public String getRemoteFileUrl();

    /**
     * Returns if the remote attachment should be copied to the local repository.
     * If the attachment is already present in the local repo, there is no reason to copy it...
     * @return if the remote attachment should be copied to the local repository.
     */
    public boolean isCopyLocal();
}
