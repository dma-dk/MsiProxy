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

import dk.dma.msiproxy.model.LocalizedDesc;
import dk.dma.msiproxy.model.LocalizedEntity;
import dk.dma.msiproxy.model.MessageFilter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * The Point entity
 */
@XmlType(propOrder = { "descs", "lat", "lon" })
public class Point extends LocalizedEntity<Point.PointDesc> {
    Double lat;
    Double lon;
    int index;

    /**
     * Constructor
     */
    public Point() {
        super();
    }

    /**
     * Constructor
     * @param point the point
     * @param filter what type of data to include from the entity
     */
    public Point(Point point, MessageFilter filter) {
        this();

        lat = point.getLat();
        lon = point.getLon();
        index = point.getIndex();
        if (point.getDescs() != null) {
            point.getDescs(filter).stream()
                    .forEach(desc -> checkCreateDescs().add(desc));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PointDesc createDesc(String lang) {
        PointDesc desc = new PointDesc();
        desc.setLang(lang);
        checkCreateDescs().add(desc);
        return desc;
    }


    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    @XmlTransient
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @XmlTransient
    public boolean isDefined() {
        return lat != null && lon != null;
    }

    @Override
    @XmlElement // Make JAX-B Happy
    public List<PointDesc> getDescs() { return  super.getDescs(); }

    /**
     * The Point description entity
     */
    @XmlType(propOrder = {"lang", "description"})
    public static class PointDesc extends LocalizedDesc {

        String description;

        /**
         * Constructor
         */
        public PointDesc() {
            super();
        }

        @Override
        @XmlElement // Make JAX-B Happy
        public String getLang() {
            return super.getLang();
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean descDefined() {
            return fieldsDefined(description);
        }
    }
}
