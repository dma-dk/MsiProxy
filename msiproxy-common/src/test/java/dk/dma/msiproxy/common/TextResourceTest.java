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

import dk.dma.msiproxy.common.conf.TextResource;
import dk.dma.msiproxy.common.conf.TextResourceProducer;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * Unit tests for the Settings
 */
@RunWith(CdiRunner.class)
@AdditionalClasses(value = {
        TextResourceProducer.class
})
public class TextResourceTest {

    @Inject
    @TextResource("/text_resource_test.txt")
    String textResource0;

    @Test
    public void textResourceTest() {

        // Test text resource loaded from text_resource_test.txt
        Assert.assertEquals("hello mum!", textResource0.trim());
    }
}
