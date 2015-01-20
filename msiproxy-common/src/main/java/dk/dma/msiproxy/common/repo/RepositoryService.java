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

import dk.dma.msiproxy.common.MsiProxyApp;
import dk.dma.msiproxy.common.settings.annotation.Setting;
import dk.dma.msiproxy.common.util.WebUtils;
import dk.dma.msiproxy.model.msi.Attachment;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A repository service.<br>
 * Streams files from the repository.
 * <p>
 *     The repository is public in as much as everybody can download all files.<br>
 * </p>
 */
@javax.ws.rs.Path("/repo")
@Singleton
@Lock(LockType.READ)
public class RepositoryService {

    @Context
    ServletContext servletContext;

    @Inject
    @Setting(value = "repoRootPath", defaultValue = "${user.home}/.msiproxy/repo", substituteSystemProperties = true)
    Path repoRoot;

    @Inject
    @Setting(value = "repoCacheTimeoutMinutes", defaultValue = "5")
    Long cacheTimeout;

    @Inject
    Logger log;

    @Inject
    FileTypes fileTypes;

    @Inject
    ThumbnailService thumbnailService;

    @Inject
    MsiProxyApp app;

    /**
     * Initializes the repository
     */
    @PostConstruct
    public void init() {
        // Create the repo root directory
        if (!Files.exists(getRepoRoot())) {
            try {
                Files.createDirectories(getRepoRoot());
            } catch (IOException e) {
                log.error("Error creating repository dir " + getRepoRoot(), e);
            }
        }

        // Create the repo "temp" root directory
        if (!Files.exists(getTempRepoRoot())) {
            try {
                Files.createDirectories(getTempRepoRoot());
            } catch (IOException e) {
                log.error("Error creating repository dir " + getTempRepoRoot(), e);
            }
        }
    }

    /**
     * Returns the repository root
     * @return the repository root
     */
    public Path getRepoRoot() {
        return repoRoot;
    }

    /**
     * Returns the repository "temp" root
     * @return the repository "temp" root
     */
    public Path getTempRepoRoot() {
        return getRepoRoot().resolve("temp");
    }


    /**
     * Creates a URI from the repo file
     * @param repoFile the repo file
     * @return the URI for the file
     */
    public String getRepoUri(Path repoFile) {
        Path filePath = getRepoRoot().relativize(repoFile);
        return "/rest/repo/file/" + WebUtils.encodeURI(filePath.toString().replace('\\', '/'));
    }

    /**
     * Creates a path from the repo file relative to the repo root
     * @param repoFile the repo file
     * @return the path for the file
     */
    public String getRepoPath(Path repoFile) {
        Path filePath = getRepoRoot().relativize(repoFile);
        return filePath.toString().replace('\\', '/');
    }

    /**
     * Creates two levels of sub-folders within the {@code rootFolder} based on
     * a MD5 hash of the {@code target}.
     *
     * @param rootFolder the root folder within the repository root
     * @param target the target name used for the hash
     * @param includeTarget whether to create a sub-folder for the target or not
     * @param createFolders whether or not to create the folders
     * @return the sub-folder associated with the target
     */
    public Path getHashedSubfolder(String rootFolder, String target, boolean includeTarget, boolean createFolders) throws IOException {
        byte[] bytes = target.getBytes("utf-8");

        // MD5 hash the ID
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("This should never happen");
        }
        md.update(bytes);
        bytes = md.digest();
        String hash = String.valueOf(Integer.toHexString(bytes[0] & 0xff));
        while (hash.length() < 2) {
            hash = "0" + hash;
        }

        Path folder = getRepoRoot();

        // Add the root folder
        if (StringUtils.isNotBlank(rootFolder)) {
            folder = folder.resolve(rootFolder);
        }

        // Add two hashed sub-folder levels
        folder = folder
                .resolve(hash.substring(0, 1))
                .resolve(hash.substring(0, 2));

        // Check if we should create a sub-folder for the target as well
        if (includeTarget) {
            folder = folder.resolve(target);
        }

        // Create the folder if it does not exist
        if (createFolders && !Files.exists(folder)) {
            Files.createDirectories(folder);
        }
        return folder;
    }


    /**
     * Streams the file specified by the path
     * @param path the path
     * @param request the servlet request
     * @return the response
     */
    @GET
    @javax.ws.rs.Path("/file/{file:.+}")
    public Response streamFile(@PathParam("file") String path,
                               @Context Request request) throws IOException {

        Path f = repoRoot.resolve(path);

        if (Files.notExists(f) || Files.isDirectory(f)) {
            log.warn("Failed streaming file: " + f);
            return Response
                    .status(404)
                    .build();
        }

        // Set expiry to cacheTimeout minutes
        Date expirationDate = new Date(System.currentTimeMillis() + 1000L * 60L * cacheTimeout);

        String mt = fileTypes.getContentType(f);

        // Check for an ETag match
        EntityTag etag = new EntityTag("" + Files.getLastModifiedTime(f).toMillis() + "_" + Files.size(f), true);
        Response.ResponseBuilder responseBuilder = request.evaluatePreconditions(etag);
        if (responseBuilder != null) {
            // Etag match
            log.trace("File unchanged. Return code 304");
            return responseBuilder
                    .expires(expirationDate)
                    .build();
        }

        log.trace("Streaming file: " + f);
        return Response
                .ok(f.toFile(), mt)
                .expires(expirationDate)
                .tag(etag)
                .build();
    }

    /**
     * Returns the thumbnail to use for the file specified by the path
     * @param path the path
     * @param size the icon size, either 32, 64 or 128
     * @return the thumbnail to use for the file specified by the path
     */
    @GET
    @javax.ws.rs.Path("/thumb/{file:.+}")
    public Response getThumbnail(@PathParam("file") String path,
                                 @QueryParam("size") @DefaultValue("64") int size) throws IOException, URISyntaxException {

        IconSize iconSize = IconSize.getIconSize(size);
        Path f = repoRoot.resolve(path);

        if (Files.notExists(f) || Files.isDirectory(f)) {
            log.warn("Failed streaming file: " + f);
            throw new WebApplicationException(404);
        }

        // Check if we can generate a thumbnail for image files
        String thumbUri = null;
        Path thumbFile = thumbnailService.getThumbnail(f, iconSize);
        if (thumbFile != null) {
            thumbUri = app.getBaseUri() + getRepoUri(thumbFile);
        } else {
            // Fall back to file type icons
            thumbUri = app.getBaseUri() + "/" + fileTypes.getIcon(f, iconSize);
        }

        log.trace("Redirecting to thumbnail: " + thumbUri);
        return Response
                .temporaryRedirect(new URI(thumbUri))
                .build();
    }

    /**
     * Returns a list of files in the folder specified by the path
     * @param path the path
     * @return the list of files in the folder specified by the path
     */
    @GET
    @javax.ws.rs.Path("/list/{folder:.+}")
    @Produces("application/json;charset=UTF-8")
    @NoCache
    public List<Attachment> listFiles(@PathParam("folder") String path) throws IOException {

        List<Attachment> result = new ArrayList<>();
        Path folder = repoRoot.resolve(path);

        if (Files.exists(folder) && Files.isDirectory(folder)) {

            // Filter out directories, hidden files, thumbnails and map images
            DirectoryStream.Filter<Path> filter = file ->
                    Files.isRegularFile(file) &&
                            !file.getFileName().toString().startsWith(".") &&
                            !file.getFileName().toString().matches(".+_thumb_\\d{1,3}\\.\\w+") && // Thumbnails
                            !file.getFileName().toString().matches("map_\\d{1,3}\\.png"); // Map image

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, filter)) {
                stream.forEach(f -> {
                            Attachment vo = new Attachment();
                            vo.setName(f.getFileName().toString());
                            vo.setPath(WebUtils.encodeURI(path + "/" + f.getFileName().toString()));
                            vo.setDirectory(Files.isDirectory(f));
                            try {
                                vo.setUpdated(new Date(Files.getLastModifiedTime(f).toMillis()));
                                vo.setSize(Files.size(f));
                            } catch (Exception e) {
                                log.trace("Error reading file attribute for " + f);
                            }
                            result.add(vo);
                        });
            }
        }
        return result;
    }

}
