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

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for a localized entity
 */
public abstract class LocalizedEntity<D extends LocalizedDesc> implements JsonSerializable {

    List<D> descs;

    /**
     * Returns the list of localized descriptions
     * @return the list of localized descriptions
     */
    public List<D> getDescs() {
        return descs;
    }

    /**
     * Sets the list of localized descriptions
     * @param descs the list of localized descriptions
     */
    public void setDescs(List<D> descs) {
        this.descs = descs;
    }

    /**
     * Returns the list of localized descriptions as specified by the data filter.
     * <p>
     *     If no description matches the filter, the first available description is included.
     * </p>
     *
     * @param filter defines the languages to include from the entity
     * @return the list of localized descriptions as specified by the data filter
     */
    public List<D> getDescs(MessageFilter filter) {
        // Sanity checks
        if (filter == null || getDescs() == null) {
            return getDescs();
        }

        // Collect the matching descriptions
        List<D> result = new ArrayList<>();
        getDescs().stream()
                .filter(desc -> filter.getLang() == null || filter.getLang().equals(desc.getLang()))
                .forEach(result::add);

        // If no match is found, pick the first available
        if (result.isEmpty() && !getDescs().isEmpty()) {
            result.add(getDescs().get(0));
        }
        return result;
    }

    /**
     * Returns the list of localized descriptions and creates the list if necessary
     * @return the list of localized descriptions
     */
    public List<D> checkCreateDescs() {
        if (getDescs() == null) {
            setDescs(new ArrayList<>());
        }
        return getDescs();
    }

    /**
     * Returns the localized description for the given language.
     * Returns null if the description is not defined.
     *
     * @param lang the language
     * @return the localized description for the given language
     */
    public D getDesc(String lang) {
        if (getDescs() != null) {
            return getDescs().stream()
                    .filter(d -> d.getLang().equalsIgnoreCase(lang))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Sorts the descriptive entities to ensure that the given language is first
     * @param lang the language to sort first
     */
    public void sortDescs(final String lang) {
        if (getDescs() != null && lang != null) {
            getDescs().sort((d1, d2) -> {
                String l1 = (d1 == null) ? null : d1.getLang();
                String l2 = (d2 == null) ? null : d2.getLang();
                if (l1 == null && l2 == null) {
                    return 0;
                } else if (l1 == null) {
                    return 1;
                } else if (l2 == null) {
                    return -1;
                } else if (l1.equals(l2)) {
                    return 0;
                } else {
                    return l1.equals(lang) ? -1 : (l2.equals(lang) ? 1 : 0);
                }
            });
        }
    }
}
