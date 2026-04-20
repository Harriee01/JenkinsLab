package com.fakestore.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JsonUtils – thin wrapper around Jackson's {@link ObjectMapper}.
 *
 * <p>Provides static helpers to convert Java objects to JSON strings and
 * vice-versa, centralising error handling + logging so test code stays clean.
 */
public final class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    /** Shared, thread-safe ObjectMapper instance. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Utility class – no instantiation
    private JsonUtils() {}

    /**
     * Serialises any Java object to a compact JSON string.
     *
     * @param object the value to serialise
     * @return JSON string representation
     * @throws RuntimeException wrapping {@link JsonProcessingException} on failure
     */
    public static String toJson(Object object) {
        try {
            String json = MAPPER.writeValueAsString(object);
            log.debug("Serialised object to JSON: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            log.error("Failed to serialise object to JSON: {}", object, e);
            throw new RuntimeException("JSON serialisation failed", e);
        }
    }

    /**
     * Deserialises a JSON string into an instance of the given class.
     *
     * @param json      JSON string to parse
     * @param valueType target class
     * @param <T>       target type parameter
     * @return deserialised Java object
     * @throws RuntimeException wrapping {@link JsonProcessingException} on failure
     */
    public static <T> T fromJson(String json, Class<T> valueType) {
        try {
            T result = MAPPER.readValue(json, valueType);
            log.debug("Deserialised JSON to {}: {}", valueType.getSimpleName(), result);
            return result;
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialise JSON to {}: {}", valueType.getSimpleName(), json, e);
            throw new RuntimeException("JSON deserialisation failed", e);
        }
    }

    /** Returns the shared {@link ObjectMapper} for advanced use-cases in tests. */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
