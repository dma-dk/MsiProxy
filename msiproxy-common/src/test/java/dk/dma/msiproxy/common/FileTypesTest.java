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
