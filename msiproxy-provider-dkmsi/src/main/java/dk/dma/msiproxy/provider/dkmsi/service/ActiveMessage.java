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
package dk.dma.msiproxy.provider.dkmsi.service;

import dk.dma.msiproxy.common.util.TimeUtils;
import dk.dma.msiproxy.model.msi.Message;

import java.util.Date;
import java.util.Objects;

/**
 * Represents an active legacy MSI or Firing exercise as read in from the database
 */
public class ActiveMessage {

    public static final String MSI = "MSI";
    public static final String FIRING_EXERCISE = "FE";

    Integer id;
    String type;
    Date updated;
    Date validFrom;
    Date validTo;

    /**
     * Construct the active message from a DB row
     * @param row
     */
    public ActiveMessage(Object[] row) {
        type = (String)row[0];
        id = (Integer)row[1];
        updated = (Date)row[2];
        validFrom = (Date)row[3];
        validTo = (Date)row[4];
    }

    /**
     * Returns if this message represents a legacy firing exercise
     * @return if this message represents a legacy firing exercise
     */
    public boolean isFiringExercise() {
        return FIRING_EXERCISE.equals(type);
    }

    /**
     * Returns if this message represents a legacy MSI
     * @return if this message represents a legacy MSI
     */
    public boolean isMsi() {
        return MSI.equals(type);
    }

    /**
     * Returns if this MSI or Firing Exercise is identical to the given message
     * by comparing ID and dates.
     *
     * @param msg the message to compare this message to
     * @return if the message is unchanged
     */
    public boolean isUnchanged(Message msg) {
        if (isMsi()) {
            // Compare legacy MSI
            return Objects.equals(id, msg.getId()) &&
                    Objects.equals(updated, msg.getUpdated());
        } else {
            // Compare legacy Firing Exercises
            // Sadly, legacy Firing Exercises do not have a change date
            return Objects.equals(id, msg.getId()) &&
                    Objects.equals(TimeUtils.resetSeconds(validFrom), msg.getValidFrom()) &&
                    Objects.equals(TimeUtils.resetSeconds(validTo), msg.getValidTo());
        }
    }

    // ********* Getters and setters *******

    public Integer getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Date getUpdated() {
        return updated;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }
}
