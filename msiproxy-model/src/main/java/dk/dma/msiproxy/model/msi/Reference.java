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
package dk.dma.msiproxy.model.msi;

import dk.dma.msiproxy.model.JsonSerializable;

/**
 * The Reference entity
 */
public class Reference implements JsonSerializable {

    SeriesIdentifier seriesIdentifier;
    ReferenceType type;

    /**
     * Constructor
     */
    public Reference() {
    }

    public SeriesIdentifier getSeriesIdentifier() {
        return seriesIdentifier;
    }

    public void setSeriesIdentifier(SeriesIdentifier seriesIdentifier) {
        this.seriesIdentifier = seriesIdentifier;
    }

    public ReferenceType getType() {
        return type;
    }

    public void setType(ReferenceType type) {
        this.type = type;
    }
}
