package dk.dma.msiproxy.web;

import dk.dma.msiproxy.common.util.PositionFormatter;
import dk.dma.msiproxy.common.util.TextUtils;
import dk.dma.msiproxy.model.msi.Area;
import dk.dma.msiproxy.model.msi.Message;
import org.apache.commons.lang.StringUtils;

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
     * Returns the area lineage to display for an area. If areaHeading is defined
     * this is excluded from the lineage.
     * @param area the area
     * @param areaHeading the current area heading
     * @return the area lineage to display for an area
     */
    public static String getAreaLineage(Area area, Area areaHeading) {
        String result = "";
        for (; area != null && (areaHeading == null || !areaHeading.getId().equals(area.getId())); area = area.getParent()) {
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
     * Returns the message title line, composed of the area lineage, the vicinity and the message title
     * @param msg the message to return the title for
     * @param areaHeading the current area heading
     * @return the message title line
     */
    public static String getMessageTitleLine(Message msg, Area areaHeading) {
        StringBuilder result = new StringBuilder();
        result.append(getAreaLineage(msg.getArea(), areaHeading));
        if (msg.getDescs() != null && msg.getDescs().size() > 0) {
            Message.MessageDesc desc = msg.getDescs().get(0);
            appendPart(result, desc.getVicinity());
            appendPart(result, desc.getTitle());
        }
        return result.toString();
    }

    /**
     * Appends the given part to the string separated using a "-" divider character
     * @param str the string to append to
     * @param part the part to append
     */
    private static void appendPart(StringBuilder str, String part) {
        if (StringUtils.isNotBlank(part)) {
            if (str.length() > 0) {
                str.append(" - ");
            }
            str.append(part);
        }
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

    /**
     * Converts the given plain text to HTML
     * @param txt the plain text
     * @return the HTML
     */
    public static String txt2html(String txt) {
        return TextUtils.txt2html(txt);
    }
}
