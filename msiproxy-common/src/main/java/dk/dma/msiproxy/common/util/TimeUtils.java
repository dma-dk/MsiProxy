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
package dk.dma.msiproxy.common.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Utility methods for handling date and time.
 */
public class TimeUtils {

    /**
     * Resets the time part of the date to 0:0:0
     * @param cal the date to reset
     * @return the reset date
     */
    public static Calendar resetTime(Calendar cal) {
        if (cal != null) {
            cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
            cal.set(Calendar.MINUTE, 0);                 // set minute in hour
            cal.set(Calendar.SECOND, 0);                 // set second in minute
            cal.set(Calendar.MILLISECOND, 0);            // set millis in second
        }
        return cal;
    }

    /**
     * Resets the time part of the date to 0:0:0
     * @param date the date to reset
     * @return the reset date
     */
    public static Date resetTime(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();       // get calendar instance
            cal.setTime(date);                           // set cal to date
            date = resetTime(cal).getTime();
        }
        return date;
    }

    /**
     * Resets the seconds part of the date to 0
     * @param cal the date to reset
     * @return the reset date
     */
    public static Calendar resetSeconds(Calendar cal) {
        if (cal != null) {
            cal.set(Calendar.SECOND, 0);                 // set second in minute
            cal.set(Calendar.MILLISECOND, 0);            // set millis in second
        }
        return cal;
    }

    /**
     * Resets the seconds part of the date to 0
     * @param date the date to reset
     * @return the reset date
     */
    public static Date resetSeconds(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();       // get calendar instance
            cal.setTime(date);                           // set cal to date
            date = resetSeconds(cal).getTime();
        }
        return date;
    }

    /**
     * Checks if the two Dates is for the same date
     * @param date1 the first date
     * @param date2 the second date
     * @return if the two Dates is for the same date
     */
    public static boolean sameDate(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return resetTime(date1).getTime() == resetTime(date2).getTime();
    }

    /**
     * Checks if the two Dates is for the same date, hour and minute
     * @param date1 the first date
     * @param date2 the second date
     * @return if the two Dates is for the same date, hour and minute
     */
    public static boolean sameDateHourMinute(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return resetSeconds(date1).getTime() == resetSeconds(date2).getTime();
    }
}
