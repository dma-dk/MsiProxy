package dk.dma.msiproxy.web;

import dk.dma.msiproxy.common.util.PositionFormatter;
import dk.dma.msiproxy.model.msi.Area;
import dk.dma.msiproxy.model.msi.Message;

import java.util.Locale;

/**
 * Defines a set of TLD functions that may be used on a JSP page
 */
public class TldFunctions {

    /**
     * Returns the area heading to display for a message
     * @param msg the message
     * @return the area heading to display for a message
     */
    public static Area getAreaHeading(Message msg) {
        Area area = msg.getArea();
        while (area != null && area.getParent() != null && area.getParent().getParent() != null) {
            area = area.getParent();
        }
        return area;
    }

    /**
     * Returns the area lineage to display for an area
     * @param area the area
     * @return the area lineage to display for an area
     */
    public static String getAreaLineage(Area area) {
        String result = "";
        for (; area != null; area = area.getParent()) {
            if (area.getDescs().size() > 0) {
                if (result.length() > 0) {
                    result = " - " + result;
                }
                result = area.getDescs().get(0).getName() + result;
            }
        }
        return result;
    }

    /**
     * Formats the lat-lon position in the given locale and format
     * @param locale the locale
     * @param format the format, either "dec" or "sec" for decimal and second formats respectively
     * @param lat the latitude
     * @param lon the longitude
     * @return the formatted position
     */
    public static String formatPos(Locale locale, String format, Double lat, Double lon) {
        PositionFormatter.Format fmt = ("sec".equalsIgnoreCase(format)) ? PositionFormatter.LATLON_SEC : PositionFormatter.LATLON_DEC;
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        StringBuilder pos = new StringBuilder();
        if (lat != null) {
            pos.append(PositionFormatter.format(locale, fmt.getLatFormat(), lat));
        }
        if (lon != null) {
            if (pos.length() > 0) {
                pos.append(" ");
            }
            pos.append(PositionFormatter.format(locale, fmt.getLonFormat(), lon));
        }
        return pos.toString();
    }
}
