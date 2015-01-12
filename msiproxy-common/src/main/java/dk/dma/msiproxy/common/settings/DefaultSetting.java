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
package dk.dma.msiproxy.common.settings;

import java.util.Objects;

/**
 * Default implementation of a setting
 */
public class DefaultSetting implements Setting {

    String settingName;
    String defaultValue;
    boolean substituteSystemProperties;

    /**
     * Designated constructor
     */
    public DefaultSetting(String settingName, String defaultValue, boolean substituteSystemProperties) {
        Objects.requireNonNull(settingName);

        this.settingName = settingName;
        this.defaultValue = defaultValue;
        this.substituteSystemProperties = substituteSystemProperties;
    }

    public DefaultSetting(String settingName, String defaultValue) {
        this(settingName, defaultValue, false);
    }

    public DefaultSetting(String settingName) {
        this(settingName, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSettingName() {
        return settingName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String defaultValue() {
        return defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean substituteSystemProperties() {
        return substituteSystemProperties;
    }
}
