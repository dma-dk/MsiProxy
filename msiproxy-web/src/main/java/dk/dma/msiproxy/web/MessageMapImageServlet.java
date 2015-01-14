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
package dk.dma.msiproxy.web;

import dk.dma.msiproxy.common.MsiProxyApp;
import dk.dma.msiproxy.common.provider.AbstractProviderService;
import dk.dma.msiproxy.common.provider.Providers;
import dk.dma.msiproxy.model.msi.Location;
import dk.dma.msiproxy.model.msi.Message;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Returns and caches a thumbnail image for a message.
 * <p></p>
 * Can be used e.g. for a grid layout in search results.
 */
@WebServlet(value = "/message-map-image/*", asyncSupported = true)
public class MessageMapImageServlet extends HttpServlet {

    static final String IMAGE_PLACEHOLDER = "/img/map_image_placeholder.png";
    static final Pattern URI_PATTERN = Pattern.compile("/(\\S+)/(\\d+)\\.png");

    private static Image msiImage, nmImage;

    @Inject
    Logger log;

    @Inject
    Providers providers;

    @Inject
    MsiProxyApp app;

    @Inject
    MapImageProducer mapImageProducer;

    /**
     * Main GET method
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Get the provider ID and message ID from the request
            Matcher m = URI_PATTERN.matcher(request.getPathInfo());
            if (!m.matches()) {
                throw new IllegalArgumentException("Invalid map image URI: " + request.getPathInfo());
            }
            String providerId = m.group(1);
            int id = Integer.valueOf(m.group(2));

            // Look up the associated provider
            AbstractProviderService providerService = providers.getProvider(providerId);
            if (providerService == null) {
                throw new IllegalArgumentException("Invalid MSI provider: " + providerId);
            }

            // Construct the image file name for the message
            String imageName = String.format("map_%d.png", mapImageProducer.getMapImageSize());

            // Compute the path and URI for the image file
            Path messageRepoFolder = providerService.getMessageRepoFolder(id);
            Path imageRepoPath = messageRepoFolder.resolve(imageName);
            String uri = providerService.getMessageFileRepoUri(id, imageName);
            boolean imageFileExists = Files.exists(imageRepoPath);

            // Look up the message
            Message message = providerService.getMessage(id);

            // Handle the case where the message does not exist
            if (message == null) {
                // This may be because the message is not active anymore. Check if the image still exists
                if (imageFileExists) {
                    response.sendRedirect(uri);
                    return;
                }
                // No joy. Report an error
                throw new IllegalArgumentException("Message " + id + " does not exist");
            }

            // If the modification date of the message matches the image file - use it
            if (imageFileExists && message.getUpdated().getTime() <= Files.getLastModifiedTime(imageRepoPath).toMillis()) {
                response.sendRedirect(uri);
                return;
            }

            // We need to construct hte image from the message locations
            List<Location> locations = getMessageLocations(message);
            if (locations.size() > 0) {

                imageFileExists = mapImageProducer.createMapImage(
                        locations,
                        imageRepoPath,
                        getMessageImage(message),
                        message.getUpdated());

                // Either return the image file, or a place holder image
                if (imageFileExists) {
                    // Redirect the the repository streaming service
                    response.sendRedirect(uri);
                    return;
                }
            }

        } catch (Exception ex) {
            log.warn("Error fetching map image for message: " + ex);
        }

        // Show a placeholder image
        response.sendRedirect(IMAGE_PLACEHOLDER);
    }

    /**
     * Extracts the locations from the message
     * @param message the message
     * @return the list of locations
     */
    public List<Location> getMessageLocations(Message message) {
        List<Location> result = new ArrayList<>();
        if (message != null) {
            result.addAll(message.getLocations()
                    .stream()
                    .filter(location -> location.getPoints().size() > 0)
                    .collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * Depending on the type of message, return an MSI or an NM image
     * @param message the  message
     * @return the corresponding image
     */
    public Image getMessageImage(Message message) {
        return message.getType().isMsi() ? getMsiImage() : getNmImage();
    }

    /**
     * Returns the MSI symbol image
     * @return the MSI symbol image
     */
    private synchronized Image getMsiImage() {
        if (msiImage == null) {
            String imageUrl = app.getBaseUri() + "/img/msi.png";
            try {
                msiImage = ImageIO.read(new URL(imageUrl));
            } catch (IOException e) {
                log.error("This should never happen - could not load image from " + imageUrl);
            }
        }
        return msiImage;
    }


    /**
     * Returns the MSI symbol image
     * @return the MSI symbol image
     */
    private synchronized Image getNmImage() {
        if (nmImage == null) {
            String imageUrl = app.getBaseUri() + "/img/nm.png";
            try {
                nmImage = ImageIO.read(new URL(imageUrl));
            } catch (IOException e) {
                log.error("This should never happen - could not load image from " + imageUrl);
            }
        }
        return nmImage;
    }
}
