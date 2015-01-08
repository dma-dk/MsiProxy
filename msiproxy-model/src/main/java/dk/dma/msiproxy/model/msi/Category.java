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
import java.util.ArrayList;
import java.util.List;

/**
 * The Category model entity
 */
@XmlRootElement(name = "category")
@XmlType(propOrder = {"descs", "parent"})
public class Category extends LocalizedEntity<Category.CategoryDesc> {
    Integer id;
    Category parent;
    List<Category> children;

    /**
     * Constructor
     */
    public Category() {
    }

    /**
     * Constructor
     * @param category the category
     * @param filter what type of data to include from the entity
     */
    public Category(Category category, MessageFilter filter) {
        this();

        id = category.getId();

        // NB: We only copy parent Areas, not children
        if (category.getParent() != null) {
            parent = new Category(category.getParent(), filter);
        }

        if (category.getDescs() != null) {
            category.getDescs(filter).stream()
                    .forEach(desc -> checkCreateDescs().add(desc));
        }
    }


    /**
     * Returns or creates the list of child categories
     * @return the list of child categories
     */
    public List<Category> checkCreateChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
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
    public List<CategoryDesc> getDescs() { return super.getDescs(); }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    @XmlTransient
    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }

    /**
     * The Category description entity
     */
    @XmlType(propOrder = {"lang", "name"})
    public static class CategoryDesc extends LocalizedDesc {

        String name;

        /**
         * Constructor
         */
        public CategoryDesc() {
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
