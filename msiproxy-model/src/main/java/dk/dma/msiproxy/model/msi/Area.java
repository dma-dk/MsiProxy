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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * The Area model entity
 */
@XmlRootElement(name = "area")
@XmlType(propOrder = {"descs", "parent"})
public class Area extends LocalizedEntity<Area.AreaDesc> implements Comparable<Area> {
    Integer id;
    Area parent;
    List<Area> children;
    double sortOrder;

    /**
     * Constructor
     */
    public Area() {
    }

    /**
     * Constructor
     * @param area the area
     * @param filter what type of data to include from the entity
     */
    public Area(Area area, MessageFilter filter) {
        this();

        id = area.getId();
        sortOrder = area.getSortOrder();

        // NB: We only copy parent Areas, not children
        if (area.getParent() != null) {
            parent = new Area(area.getParent(), filter);
        }

        if (area.getDescs() != null) {
            area.getDescs(filter).stream()
                    .forEach(desc -> checkCreateDescs().add(desc));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AreaDesc createDesc(String lang) {
        AreaDesc desc = new AreaDesc();
        desc.setLang(lang);
        checkCreateDescs().add(desc);
        return desc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Area area) {
        return (area == null || sortOrder == area.getSortOrder()) ? 0 : (sortOrder < area.getSortOrder() ? -1 : 1);
    }

    @XmlAttribute
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    @Override
    @XmlElement // Make JAX-B Happy
    public List<AreaDesc> getDescs() { return  super.getDescs(); }

    public Area getParent() {
        return parent;
    }

    public void setParent(Area parent) {
        this.parent = parent;
    }

    @XmlTransient
    public List<Area> getChildren() {
        return children;
    }

    public void setChildren(List<Area> children) {
        this.children = children;
    }

    @XmlTransient
    public double getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(double sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * The entity description for an area
     */
    @XmlType(propOrder = {"lang", "name"})
    public static class AreaDesc extends LocalizedDesc {

        String name;

        /**
         * Constructor
         */
        public AreaDesc() {
            super();
        }

        @Override
        @XmlElement // Make JAX-B Happy
        public String getLang() {
            return super.getLang();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean descDefined() {
            return fieldsDefined(name);
        }
    }

}
