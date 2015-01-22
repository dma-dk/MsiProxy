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

import dk.dma.msiproxy.common.repo.FileTypes;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;

/**
 * Unit tests for the FileTypes
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        FileTypes.class
})
public class FileTypesTest {

    @Inject
    FileTypes fileTypes;


    @Test
    public void fileTypeTest() {

        String filePath = getClass().getResource("/tycho-brahe-stjerneborg.jpg").getFile();
        Assert.assertNotEquals(filePath, "");

        File file = new File(filePath);
        Assert.assertTrue(file.exists());

        Assert.assertEquals("image/jpeg", fileTypes.getContentType(file));
    }

}
