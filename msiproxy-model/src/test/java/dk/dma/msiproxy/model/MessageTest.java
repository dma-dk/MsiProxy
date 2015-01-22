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
package dk.dma.msiproxy.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dma.msiproxy.model.msi.Message;
import dk.dma.msiproxy.model.msi.SeriesIdType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Unit test for MSI messages
 */
public class MessageTest {

    Message msg;

    @Before
    public void setup() throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        msg = jsonMapper.readValue(getClass().getResourceAsStream("/message.json"), Message.class);
    }

    @Test
    public void messageTest() throws IOException {
        // Check that the message has been loaded as expected
        Assert.assertNotNull(msg);
        Assert.assertEquals(Integer.valueOf(13987), msg.getId());

        Assert.assertNotNull(msg.getSeriesIdentifier());
        Assert.assertEquals(SeriesIdType.MSI, msg.getSeriesIdentifier().getMainType());
        Assert.assertEquals("DK", msg.getSeriesIdentifier().getAuthority());
        Assert.assertEquals(Integer.valueOf(2015), msg.getSeriesIdentifier().getYear());
        Assert.assertNull(msg.getSeriesIdentifier().getNumber());

        Assert.assertNotNull(msg.getDescs());
        Assert.assertTrue(msg.getDescs().size() == 2);
        Assert.assertEquals("da", msg.getDescs().get(0).getLang());
        Assert.assertEquals("en", msg.getDescs().get(1).getLang());
        Assert.assertEquals("Molefyret på pos. 54 45,2 N - 010 40,2 E er slukket.", msg.getDesc("da").getDescription());

        Assert.assertNotNull(msg.getArea());
        Assert.assertEquals("Østersøen", msg.getArea().getDesc("da").getName());
        Assert.assertNotNull(msg.getArea().getParent());
        Assert.assertEquals("Danmark", msg.getArea().getParent().getDesc("da").getName());

        Assert.assertNotNull(msg.getCategories());
        Assert.assertTrue(msg.getCategories().size() == 1);

        Assert.assertNotNull(msg.getLocations());
        Assert.assertTrue(msg.getLocations().size() == 1);
    }

    @Test
    public void messageFilterLanguageTest() throws IOException {

        // Create a Danish filtered message
        MessageFilter filter = new MessageFilter().lang("da");
        Message daMsg = new Message(msg, filter);

        // Check that only Danish descriptions are present

        Assert.assertNotNull(daMsg.getDescs());
        Assert.assertTrue(daMsg.getDescs().size() == 1);
        Assert.assertEquals("da", daMsg.getDescs().get(0).getLang());

        Assert.assertTrue(daMsg.getArea().getDescs().size() == 1);
        Assert.assertEquals("da", daMsg.getArea().getDescs().get(0).getLang());
    }

}
