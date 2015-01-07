package dk.dma.msiproxy.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * JSON-related utility methods
 */
public class JsonUtils {

    /**
     * Parses the json data as an entity of the given class
     *
     * @param data the json data to parse
     * @param dataClass the class of the data
     * @return the parsed data
     */
    public static <T> T fromJson(String data, Class<T> dataClass) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        return jsonMapper.readValue(data, dataClass);
    }

    /**
     * Parses the json data as an entity of the given class
     *
     * @param in the json data to parse
     * @param dataClass the class of the data
     * @return the parsed data
     */
    public static <T> T fromJson(InputStream in, Class<T> dataClass) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        return jsonMapper.readValue(in, dataClass);
    }

    /**
     * Formats the entity as  json data
     *
     * @param data the entity to format
     * @return the json data
     */
    public static String toJson(Object data) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        return jsonMapper.writeValueAsString(data);
    }

}
