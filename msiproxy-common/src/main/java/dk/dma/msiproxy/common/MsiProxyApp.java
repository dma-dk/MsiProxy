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

import dk.dma.msiproxy.common.settings.DefaultSetting;
import dk.dma.msiproxy.common.settings.Setting;
import dk.dma.msiproxy.common.settings.Settings;

import javax.ejb.DependsOn;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;

/**
 * Common settings and functionality for the MsiProxy app
 */
@Singleton
@Lock(LockType.READ)
@DependsOn("Settings")
public class MsiProxyApp {

    private final static Setting BASE_URI  = new DefaultSetting("baseUri", "http://localhost:8080");

    @Inject
    Settings settings;

    /**
     * Returns the base URI used to access this application
     * @return the base URI used to access this application
     */
    public String getBaseUri() {
        return settings.get(BASE_URI);
    }
}
