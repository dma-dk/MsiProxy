package dk.dma.msiproxy.provider.dkmsi.service;

import dk.dma.msiproxy.common.conf.TextResource;
import dk.dma.msiproxy.common.service.AbstractProviderService;
import dk.dma.msiproxy.common.service.MessageCache;
import dk.dma.msiproxy.common.util.TextUtils;
import dk.dma.msiproxy.model.msi.Area;
import dk.dma.msiproxy.model.msi.Category;
import dk.dma.msiproxy.model.msi.Message;
import dk.dma.msiproxy.model.msi.SeriesIdType;
import dk.dma.msiproxy.model.msi.SeriesIdentifier;
import dk.dma.msiproxy.model.msi.Status;
import dk.dma.msiproxy.model.msi.Type;
import dk.dma.msiproxy.provider.dkmsi.conf.DkMsiDB;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides a business interface for accessing Danish legacy MSI messages
 */
@Singleton
@Lock(LockType.READ)
@Startup
public class DkMsiProviderService extends AbstractProviderService {

    public static final String PROVIDER_ID = "dkmsi";

    @Inject
    Logger log;

    @Inject
    MessageCache messageCache;

    @Inject
    @DkMsiDB
    EntityManager em;

    @Inject
    @TextResource("/sql/active_msi_list.sql")
    private String activeMsiListSql;

    @Inject
    @TextResource("/sql/msi_data.sql")
    private String msiDateSql;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageCache getMessageCache() {
        return messageCache;
    }

    /**
     * Called at start up.
     */
    @PostConstruct
    public void init() {
        // Load messages
        loadMessages();
    }

    /**
     * Called every 5 minutes to update message list
     */
    @Schedule(persistent=false, second="38", minute="*/5", hour="*", dayOfWeek="*", year="*")
    protected void loadMessagesPeriodically() {
        loadMessages();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> loadMessages() {

        long t0 = System.currentTimeMillis();
        try {

            // Load the id and change data of all active MSI
            @SuppressWarnings("unchecked")
            List<Object[]> activeMsi = em
                    .createNativeQuery(activeMsiListSql)
                    .getResultList();

            // Check if there are any changes to the current list of messages
            if (activeMsi.size() == messages.size()) {

                // Check that the message ids and change dates of the two lists are identical
                boolean changes = false;
                for (int x = 0; !changes && x < messages.size(); x++) {
                    Message msg = messages.get(x);
                    if (!Objects.equals(msg.getId(), activeMsi.get(x)[0]) ||
                            !Objects.equals(msg.getUpdated(), activeMsi.get(x)[1])) {
                        changes = true;
                    }
                }

                // No changes detected
                if (!changes) {
                    log.info("Messages not changed");
                    return messages;
                }
            }


            // Load the new messages
            String ids = activeMsi.stream().map(o -> o[0].toString()).collect(Collectors.joining(","));
            String sql = msiDateSql
                    .replace(":ids", ids);
            @SuppressWarnings("unchecked")
            List<Object[]> msiData = em
                    .createNativeQuery(sql)
                    .getResultList();

            List<Message> result = readMsiData(msiData);

            setActiveMessages(result);

        } catch (Exception e) {
            log.error("Failed loading messages: " + e.getMessage());
        }

        return messages;
    }

    private List<Message> readMsiData(List<Object[]> msiData) {

        List<Message> result = new ArrayList<>();

        for (int x = 0; x < msiData.size(); x++) {

            Object[] row = msiData.get(x);
            int col = 0;

            Integer id                  = getInt(row, col++);
            Integer messageId           = getInt(row, col++);
            Boolean statusDraft         = getBoolean(row, col++);
            String  navtexNo            = getString(row, col++);
            String  descriptionEn       = getString(row, col++);
            String  descriptionDa       = getString(row, col++);
            String  title               = getString(row, col++);
            Date    validFrom           = getDate(row, col++);
            Date    validTo             = getDate(row, col++);
            Date    created             = getDate(row, col++);
            Date    updated             = getDate(row, col++);
            Date    deleted             = getDate(row, col++);
            Integer version             = getInt(row, col++);
            String  priority            = getString(row, col++);
            String  messageType         = getString(row, col++);
            String  category1En         = getString(row, col++);
            String  category1Da         = getString(row, col++);
            String  category2En         = getString(row, col++);
            String  category2Da         = getString(row, col++);
            String  area1En             = getString(row, col++);
            String  area1Da             = getString(row, col++);
            String  area2En             = getString(row, col++);
            String  area2Da             = getString(row, col++);
            String  area3En             = getString(row, col++);
            String  area3Da             = getString(row, col++);
            String  locationType        = getString(row, col++);
            Integer pointIndex          = getInt(row, col++);
            Double  pointLatitude       = getDouble(row, col++);
            Double  pointLongitude      = getDouble(row, col++);
            Integer pointRadius         = getInt(row, col);

            Message message = new Message();
            result.add(message);

            message.setId(id);
            message.setCreated(created);
            message.setUpdated(updated);
            message.setVersion(version);
            message.setValidFrom(validFrom);
            message.setValidTo(validTo);

            SeriesIdentifier identifier = new SeriesIdentifier();
            message.setSeriesIdentifier(identifier);
            identifier.setMainType(SeriesIdType.MSI);
            if (StringUtils.isNotBlank(navtexNo) && navtexNo.split("-").length == 3) {
                // Extract the series identifier from the navtext number
                String[] parts = navtexNo.split("-");
                identifier.setAuthority(parts[0]);
                identifier.setNumber(Integer.valueOf(parts[1]));
                identifier.setYear(2000 + Integer.valueOf(parts[2]));

            } else {
                // Some legacy MSI do not have a navtex number.
                Calendar cal = Calendar.getInstance();
                cal.setTime(validFrom);
                int year = cal.get(Calendar.YEAR);
                identifier.setAuthority("DK");
                identifier.setYear(year);
            }

            if ("Navtex".equals(messageType) || "Navwarning".equals(messageType)) {
                message.setType(Type.SUBAREA_WARNING);
            } else {
                message.setType(Type.COASTAL_WARNING);
            }

            Date now = new Date();
            Status status = Status.PUBLISHED;
            if (deleted != null && statusDraft) {
                status = Status.DELETED;
            } else if (deleted != null && validTo != null && deleted.after(validTo)) {
                status = Status.EXPIRED;
            } else if (deleted != null) {
                status = Status.CANCELLED;
            } else if (statusDraft) {
                status = Status.DRAFT;
            } else if (validTo != null && now.after(validTo)) {
                status = Status.EXPIRED;
            }
            message.setStatus(status);

            // Message Desc
            if (StringUtils.isNotBlank(title) || StringUtils.isNotBlank(descriptionEn) || StringUtils.isNotBlank(area3En)) {
                Message.MessageDesc descEn = message.checkCreateDesc("en");
                descEn.setTitle(StringUtils.defaultString(title, descriptionEn));
                descEn.setDescription(TextUtils.txt2html(descriptionEn));
                descEn.setVicinity(area3En);
            }
            if (StringUtils.isNotBlank(title) || StringUtils.isNotBlank(descriptionDa) || StringUtils.isNotBlank(area3Da)) {
                Message.MessageDesc descDa = message.checkCreateDesc("da");
                descDa.setTitle(StringUtils.defaultString(title, descriptionDa));
                descDa.setDescription(TextUtils.txt2html(descriptionDa));
                descDa.setVicinity(area3Da);
            }

            // Areas
            Area area = createAreaTemplate(area1En, area1Da, null);
            // Annoyingly, legacy data has Danmark as a sub-area of Danmark
            if (!StringUtils.equals(area1En, area2En) || !StringUtils.equals(area1Da, area2Da)) {
                area = createAreaTemplate(area2En, area2Da, area);
            }
            message.setArea(area);

        }

        return result;
    }

    /**
     * Creates an Area template based on the given Danish and English name
     * and optionally a parent Area
     * @param nameEn English name
     * @param nameDa Danish name
     * @param parent parent area
     * @return the Area template, or null if the names are empty
     */
    public static Area createAreaTemplate(String nameEn, String nameDa, Area parent) {
        Area area = null;
        if (StringUtils.isNotBlank(nameEn) || StringUtils.isNotBlank(nameDa)) {
            area = new Area();
            if (StringUtils.isNotBlank(nameEn)) {
                area.createDesc("en").setName(nameEn);
            }
            if (StringUtils.isNotBlank(nameDa)) {
                area.createDesc("da").setName(nameDa);
            }
            area.setParent(parent);
        }
        return area;
    }

    /**
     * Creates an Category template based on the given Danish and English name
     * and optionally a parent Category
     * @param nameEn English name
     * @param nameDa Danish name
     * @param parent parent area
     * @return the Category template, or null if the names are empty
     */
    public static Category createCategoryTemplate(String nameEn, String nameDa, Category parent) {
        Category category = null;
        if (StringUtils.isNotBlank(nameEn) || StringUtils.isNotBlank(nameDa)) {
            category = new Category();
            if (StringUtils.isNotBlank(nameEn)) {
                category.createDesc("en").setName(nameEn);
            }
            if (StringUtils.isNotBlank(nameDa)) {
                category.createDesc("da").setName(nameDa);
            }
            category.setParent(parent);
        }
        return category;
    }

    private String getString(Object[] row, int index) {
        return (String)row[index];
    }

    private Integer getInt(Object[] row, int index) {
        return (Integer)row[index];
    }

    private Double getDouble(Object[] row, int index) {
        return (Double)row[index];
    }

    private Date getDate(Object[] row, int index) {
        return (Date)row[index];
    }

    private Boolean getBoolean(Object[] row, int index) {
        return (Boolean)row[index];
    }

}
