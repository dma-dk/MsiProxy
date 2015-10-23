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
package dk.dma.msiproxy.common.settings;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * Interface for accessing settings.
 * <p/>
 * This bean can either be injected directly,
 * or the {@code @Setting} annotation can be used.
 */
@Singleton
@Lock(LockType.READ)
public class Settings {

    private final  static  String SETTINGS_FILE = "/settings.properties";

    @Inject
    private Logger log;

    Properties properties = new Properties();

    @PostConstruct
    public void loadSettingsFromPropertiesFile() {
        try {
            properties.load(getClass().getResourceAsStream(SETTINGS_FILE));
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Returns the value associated with the setting.
     * If it does not exist, it is created
     *
     * @param key the setting key
     * @return the associated value
     */
    public String get(String key) {
        return get(new DefaultSetting(key));
    }

    /**
     * Returns the value associated with the setting.
     * If it does not exist, it is created
     *
     * @param setting the source
     * @return the associated value
     */
    public String get(Setting setting) {
        Objects.requireNonNull(setting, "Must specify valid setting");

        String result;

        // If a corresponding system property is set, it takes precedence
        result = System.getProperty(setting.getSettingName());

        // Check if it has been defined in the environment
        if (result == null) {
            Map<String,String> sysmap=System.getenv();
            String t=setting.getSettingName();
            result = System.getenv(setting.getSettingName());
        }

        // Check if it has been defined in the properties file
        if (result == null) {
            result = properties.getProperty(setting.getSettingName(), setting.defaultValue());
        }

        if (result != null && setting.substituteSystemProperties()) {
            for (Object key : System.getProperties().keySet()) {
                result = result.replaceAll("\\$\\{" + key + "\\}", Matcher.quoteReplacement(System.getProperty("" + key)));
            }
        }

        return result;
    }

    /**
     * Returns the setting as a boolean
     *
     * @param setting the source
     * @return the associated value
     */
    public boolean getBoolean(Setting setting) {
        String value = get(setting);
        switch(value.toLowerCase()) {
            case "true": case "yes": case "t" : case "y":
                return true;
        }
        return false;
    }

    /**
     * Returns the setting as a long
     *
     * @param setting the source
     * @return the associated value
     */
    public long getLong(Setting setting) {
        String value = get(setting);
        return Long.valueOf(value);
    }

    /**
     * Returns the setting as a Path
     *
     * @param setting the source
     * @return the associated value
     */
    public Path getPath(Setting setting) {
        String value = get(setting);
        return Paths.get(value);
    }

    /**
     * Returns the setting as a Date
     *
     * @param setting the source
     * @return the associated value
     */
    public Date getDate(Setting setting) {
        String value = get(setting);
        return StringUtils.isBlank(value) ? new Date() : new Date(Long.valueOf(value));
    }

    /**
     * Returns the setting as a Set
     *
     * @param setting the source
     * @return the associated value
     */
    public String[] getArray(Setting setting) {
        String value = get(setting);
        return (value == null) ? new String[0] : value.split(",");
    }

    /**
     * Injects the setting defined by the {@code @Setting} annotation
     *
     * @param ip the injection point
     * @return the setting value
     */
    @Produces
    @dk.dma.msiproxy.common.settings.annotation.Setting
    public String get(InjectionPoint ip) {
        return get(ip2setting(ip));
    }

    /**
     * Injects the boolean setting defined by the {@code @Setting} annotation
     *
     * @param ip the injection point
     * @return the boolean setting value
     */
    @Produces
    @dk.dma.msiproxy.common.settings.annotation.Setting
    public boolean getBoolean(InjectionPoint ip) {
        return getBoolean(ip2setting(ip));
    }

    /**
     * Injects the Long setting defined by the {@code @Setting} annotation
     *
     * @param ip the injection point
     * @return the Long setting value
     */
    @Produces
    @dk.dma.msiproxy.common.settings.annotation.Setting
    public long getLong(InjectionPoint ip) {
        return getLong(ip2setting(ip));
    }

    /**
     * Injects the Path setting defined by the {@code @Setting} annotation
     *
     * @param ip the injection point
     * @return the Path setting value
     */
    @Produces
    @dk.dma.msiproxy.common.settings.annotation.Setting
    public Path getPath(InjectionPoint ip) {
        return getPath(ip2setting(ip));
    }

    /**
     * Injects the Date setting defined by the {@code @Setting} annotation
     *
     * @param ip the injection point
     * @return the Date setting value
     */
    @Produces
    @dk.dma.msiproxy.common.settings.annotation.Setting
    public Date getDate(InjectionPoint ip) {
        return getDate(ip2setting(ip));
    }

    /**
     * Injects the String array setting defined by the {@code @Setting} annotation
     *
     * @param ip the injection point
     * @return the String array setting value
     */
    @Produces
    @dk.dma.msiproxy.common.settings.annotation.Setting
    public String[] getArray(InjectionPoint ip) {
        return getArray(ip2setting(ip));
    }

    /**
     * Converts the injection point into the associated setting
     *
     * @param ip the injection point
     * @return the associated setting
     */
    private Setting ip2setting(InjectionPoint ip) {
        dk.dma.msiproxy.common.settings.annotation.Setting ann =
                ip.getAnnotated().getAnnotation(dk.dma.msiproxy.common.settings.annotation.Setting.class);
        String name = StringUtils.isBlank(ann.value()) ? ip.getMember().getName() : ann.value();
        return new DefaultSetting(name, ann.defaultValue(), ann.substituteSystemProperties());
    }

}
