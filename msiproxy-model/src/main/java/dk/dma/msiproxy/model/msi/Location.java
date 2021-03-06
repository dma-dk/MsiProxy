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
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * The Location entity
 */
@XmlType(propOrder = { "descs", "type", "points", "radius" })
public class Location extends LocalizedEntity<Location.LocationDesc> {

    LocationType type;
    Integer radius;
    List<Point> points;

    /**
     * Constructor
     */
    public Location() {
        super();
    }

    /**
     * Constructor
     * @param location the location
     * @param filter what type of data to include from the entity
     */
    public Location(Location location, MessageFilter filter) {
        this();

        type = location.getType();
        radius = location.getRadius();
        if (location.getPoints() != null) {
            location.getPoints()
                    .forEach(point -> checkCreatePoints().add(new Point(point, filter)));
        }
        if (location.getDescs() != null) {
            location.getDescs(filter).stream()
                    .forEach(desc -> checkCreateDescs().add(desc));
        }
    }


    /**
     * Returns or creates the list of points
     * @return the list of points
     */
    public List<Point> checkCreatePoints() {
        if (points == null) {
            points = new ArrayList<>();
        }
        return points;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocationDesc createDesc(String lang) {
        LocationDesc desc = new LocationDesc();
        desc.setLang(lang);
        checkCreateDescs().add(desc);
        return desc;
    }

    @Override
    @XmlElement // Make JAX-B Happy
    public List<LocationDesc> getDescs() { return  super.getDescs(); }

    public LocationType getType() {
        return type;
    }

    public void setType(LocationType type) {
        this.type = type;
    }

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    /**
     * The Location description entity
     */
    @XmlType(propOrder = {"lang", "description"})
    public static class LocationDesc extends LocalizedDesc {

        String description;

        /**
         * Constructor
         */
        public LocationDesc() {
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
