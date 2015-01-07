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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Message model entity
 */
@XmlRootElement(name = "message")
@XmlType(propOrder = {"seriesIdentifier", "type", "status", "area", "categories", "charts", "horizontalDatum", "validFrom", "validTo",
        "locations", "descs", "cancellationDate", "references", "lightsListNumbers", "originalInformation"})
public class Message extends LocalizedEntity<Message.MessageDesc> {

    Integer id;
    Date created;
    Date updated;
    Integer version;
    SeriesIdentifier seriesIdentifier;
    Type type;
    Status status;
    Area area;
    List<Category> categories;
    List<Location> locations;
    List<Chart> charts;
    String horizontalDatum;
    Date validFrom;
    Date validTo;
    Date cancellationDate;
    Set<Reference> references;
    List<String> lightsListNumbers;
    Boolean originalInformation;

    /**
     * Constructor
     */
    public Message() {
    }

    /**
     * Sorts the descriptive entities to ensure that the given language is first
     * @param lang the language to sort first
     */
    public void sortByLang(final String lang) {
        sortDescs(lang);
        if (area != null) {
            area.sortDescs(lang);
        }
        if (categories != null) {
            categories.forEach(cat -> cat.sortDescs(lang));
        }
        if (locations != null) {
            locations.forEach(loc -> {
                loc.sortDescs(lang);
                if (loc.getPoints() != null) {
                    loc.getPoints().forEach(pt -> pt.sortDescs(lang));
                }
            });
        }
    }


    /**
     * Returns or creates the list of categories
     * @return the list of categories
     */
    public List<Category> checkCreateCategories() {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        return categories;
    }

    /**
     * Returns or creates the list of locations
     * @return the list of locations
     */
    public List<Location> checkCreateLocations() {
        if (locations == null) {
            locations = new ArrayList<>();
        }
        return locations;
    }

    /**
     * Returns or creates the list of charts
     * @return the list of charts
     */
    public List<Chart> checkCreateCharts() {
        if (charts == null) {
            charts = new ArrayList<>();
        }
        return charts;
    }

    /**
     * Returns or creates the list of references
     * @return the list of references
     */
    public Set<Reference> checkCreateReferences() {
        if (references == null) {
            references = new HashSet<>();
        }
        return references;
    }

    /**
     * Returns or creates the list of light numbers
     * @return the list of light numbers
     */
    public List<String> checkCreateLightsListNumbers() {
        if (lightsListNumbers == null) {
            lightsListNumbers = new ArrayList<>();
        }
        return lightsListNumbers;
    }

    // ************ Getters and setters *************

    @XmlAttribute
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @XmlAttribute
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @XmlAttribute
    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @XmlAttribute
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public SeriesIdentifier getSeriesIdentifier() {
        return seriesIdentifier;
    }

    public void setSeriesIdentifier(SeriesIdentifier seriesIdentifier) {
        this.seriesIdentifier = seriesIdentifier;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    @Override
    @XmlElement // Make JAX-B Happy
    public List<MessageDesc> getDescs() { return  super.getDescs(); }

    public List<Chart> getCharts() {
        return charts;
    }

    public void setCharts(List<Chart> charts) {
        this.charts = charts;
    }

    public String getHorizontalDatum() {
        return horizontalDatum;
    }

    public void setHorizontalDatum(String horizontalDatum) {
        this.horizontalDatum = horizontalDatum;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    public Date getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(Date cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public Set<Reference> getReferences() {
        return references;
    }

    public void setReferences(Set<Reference> references) {
        this.references = references;
    }

    public List<String> getLightsListNumbers() {
        return lightsListNumbers;
    }

    public void setLightsListNumbers(List<String> lightsListNumbers) {
        this.lightsListNumbers = lightsListNumbers;
    }

    public Boolean isOriginalInformation() {
        return originalInformation;
    }

    public void setOriginalInformation(Boolean originalInformation) {
        this.originalInformation = originalInformation;
    }

    /**
     * The Message description entity
     */
    @XmlType(propOrder = { "lang", "title", "description", "otherCategories", "time", "vicinity", "note", "publication", "source" })
    public static class MessageDesc extends LocalizedDesc {

        String title;
        String description;
        String otherCategories;
        String time;
        String vicinity;
        String note;
        String publication;
        String source;

        /**
         * Constructor
         */
        public MessageDesc() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean descDefined() {
            return fieldsDefined(time, description, otherCategories, time, vicinity, note, publication, source);
        }

        // ************ Getters and setters *************

        @Override
        @XmlElement // Make JAX-B Happy
        public String getLang() {
            return super.getLang();
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getOtherCategories() {
            return otherCategories;
        }

        public void setOtherCategories(String otherCategories) {
            this.otherCategories = otherCategories;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getVicinity() {
            return vicinity;
        }

        public void setVicinity(String vicinity) {
            this.vicinity = vicinity;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getPublication() {
            return publication;
        }

        public void setPublication(String publication) {
            this.publication = publication;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }
}
