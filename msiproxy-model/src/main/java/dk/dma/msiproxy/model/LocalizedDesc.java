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

import org.apache.commons.lang.StringUtils;

/**
 * Base class for the descriptive entity of a localized entity
 */
public abstract class LocalizedDesc implements JsonSerializable {

    String lang;

    /**
     * Returns the language of this descriptive entity
     * @return the language of this descriptive entity
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the language of this descriptive entity
     * @param lang the language of this descriptive entity
     */
    void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * Returns if any of the text fields is non-blank
     * @return if any of the text fields is non-blank
     */
    public abstract boolean descDefined();

    /**
     * Utility method that returns if at least one of the given fields in non-blank
     * @param fields the list of fields to check
     * @return if at least one of the given fields in non-blank
     */
    public static boolean fieldsDefined(String... fields) {
        for (String field : fields) {
            if (StringUtils.isNotBlank(field)) {
                return true;
            }
        }
        return false;
    }
}
