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
package dk.dma.msiproxy.common;

import dk.dma.msiproxy.common.conf.LogConfiguration;
import dk.dma.msiproxy.common.repo.FileTypes;
import dk.dma.msiproxy.common.repo.IconSize;
import dk.dma.msiproxy.common.repo.ThumbnailService;
import dk.dma.msiproxy.common.settings.Settings;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

/**
 * Unit tests for the ThumbnailService
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        FileTypes.class, ThumbnailService.class, Settings.class, LogConfiguration.class
})
public class ThumbnailServiceTest {

    @Inject
    ThumbnailService thumbnailService;


    @Test
    @Ignore // Setting the lastModifiedTime on Cloudbees does not seem to work
    public void thumbnailServiceTest() throws IOException {

        String filePath = getClass().getResource("/tycho-brahe-stjerneborg.jpg").getFile();
        Assert.assertNotEquals(filePath, "");

        Path path = new File(filePath).toPath();
        Assert.assertTrue(Files.exists(path));

        // To make sure a new thumbnail is created, update the timestamp of the file
        long now = System.currentTimeMillis();
        Files.setLastModifiedTime(path, FileTime.fromMillis(now));
        Assert.assertEquals(now, Files.getLastModifiedTime(path).toMillis());

        // Create a thumbnail
        Path thumbnail = thumbnailService.getThumbnail(path, IconSize.SIZE_32);

        // Ensure that it has the expected name and modified-time
        Assert.assertTrue(Files.exists(thumbnail));
        Assert.assertEquals(now, Files.getLastModifiedTime(thumbnail).toMillis());
        Assert.assertEquals(path.getParent().resolve("tycho-brahe-stjerneborg_thumb_32.jpg"), thumbnail);
    }

}
