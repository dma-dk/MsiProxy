package dk.dma.msiproxy.model;

import dk.dma.msiproxy.model.msi.Area;
import dk.dma.msiproxy.model.msi.Category;
import dk.dma.msiproxy.model.msi.Message;
import dk.dma.msiproxy.model.msi.SeriesIdType;
import dk.dma.msiproxy.model.msi.Type;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Used for filtering messages
 */
public class MessageFilter {

    boolean detailed = true;
    String lang;
    Set<SeriesIdType> mainTypes = new HashSet<>();
    Set<Type> types = new HashSet<>();
    Integer areaId;
    Integer categoryId;

    /**
     * Returns whether this filter is empty or not
     * @return whether this filter is empty or not
     */
    public boolean isEmpty() {
        return StringUtils.isBlank(lang)
                && mainTypes.isEmpty()
                && types.isEmpty()
                && areaId == null
                && categoryId == null;
    }

    /**
     * Returns a key that uniquely defines the filter
     * @return a key that uniquely defines the filter
     */
    public String getKey() {
        // Need to make sure that the types are enlisted in a deterministic order
        List<SeriesIdType> mainTypes = new ArrayList<>(this.mainTypes);
        Collections.sort(mainTypes);
        List<Type> types = new ArrayList<>(this.types);
        Collections.sort(types);
        return String.format(
                "%s_%b_%s_%s_%s_%s",
                StringUtils.defaultString(lang),
                detailed,
                mainTypes.stream().map(Enum::toString).collect(Collectors.joining("-")),
                types.stream().map(Enum::toString).collect(Collectors.joining("-")),
                areaId == null ? "" : areaId.toString(),
                categoryId == null ? "" : categoryId.toString()
                );
    }

    /**
     * Filters the list of messages according to the current filter
     * @param messages the list of messages to filter
     * @return the filtered list of messages
     */
    public List<Message> filter(List<Message> messages) {
        if (messages == null || isEmpty()) {
            return messages;
        }

        List<Message> result = new CopyOnWriteArrayList<>();
        result.addAll(
                messages.stream()
                    .filter(this::filterMessage)
                    .map(msg -> new Message(msg, this))
                    .filter(msg -> msg.getDescs() != null && msg.getDescs().size() > 0)
                    .collect(Collectors.toList())
        );
        return result;
    }

    /**
     * Returns if the message is included in the filter or not
     * @param msg the message to check
     * @return if the message is included in the filter or not
     */
    private boolean filterMessage(Message msg) {

        // Filter on main type
        boolean included = mainTypes.isEmpty() || mainTypes.contains(msg.getSeriesIdentifier().getMainType());

        // Filter on type
        included &= types.isEmpty() || types.contains(msg.getType());

        // Filter on area
        if (areaId != null) {
            boolean found = false;
            for (Area area = msg.getArea(); !found && area != null; area = area.getParent()) {
                found = Objects.equals(area.getId(), areaId);
            }
            included &= found;
        }

        // Filter on category
        if (categoryId != null) {
            boolean found = false;
            if (msg.getCategories() != null) {
                for (Iterator<Category> it = msg.getCategories().iterator(); !found && it.hasNext(); ) {
                    Category cat = it.next();
                    for (Category category = cat; !found && category != null; category = category.getParent()) {
                        found = Objects.equals(category.getId(), categoryId);
                    }
                }
            }
            included &= found;
        }

        return included;
    }

    /**
     * Sets the language to filter by
     * @param lang the language to filter by
     * @return the updated message filter
     */
    public MessageFilter lang(String lang) {
        this.lang = lang;
        return this;
    }

    /**
     * Sets whether to include detailed data or not
     * @param detailed whether to include detailed data or not
     * @return the updated message filter
     */
    public MessageFilter detailed(boolean detailed) {
        this.detailed = detailed;
        return this;
    }

    /**
     * Sets the types to filter by. The type may either be
     * one of the main types, "MSI" or "NM", or one of the sub-types,
     * "PERMANENT_NOTICE", "TEMPORARY_NOTICE", "PRELIMINARY_NOTICE", "MISCELLANEOUS_NOTICE",
     * "COASTAL_WARNING", "SUBAREA_WARNING", "NAVAREA_WARNING", "LOCAL_WARNING".
     *
     * @param types the types to filter by
     * @return the updated message filter
     */
    public MessageFilter types(String... types) {
        if (types != null && types.length > 0) {
            Arrays.asList(types).forEach(type -> {
                if (type.equals("MSI") || type.equals("NM")) {
                    this.mainTypes.add(SeriesIdType.valueOf(type));
                } else if (StringUtils.isNotBlank(type)) {
                    this.types.add(Type.valueOf(type));
                }
            });
        }
        return this;
    }

    /**
     * Sets the area ID to filter by
     * @param areaId the area to filter by
     * @return the updated message filter
     */
    public MessageFilter area(Integer areaId) {
        this.areaId = areaId;
        return this;
    }

    /**
     * Sets the category ID to filter by
     * @param categoryId the category to filter by
     * @return the updated message filter
     */
    public MessageFilter category(Integer categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public boolean isDetailed() {
        return detailed;
    }

    public String getLang() {
        return lang;
    }

    public Set<SeriesIdType> getMainTypes() {
        return mainTypes;
    }

    public Set<Type> getTypes() {
        return types;
    }

    public Integer getAreaId() {
        return areaId;
    }
}
