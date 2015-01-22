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
