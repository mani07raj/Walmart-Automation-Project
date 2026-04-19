package com.walmart.framework.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * JsonReader — Reads test data from JSON files using Gson.
 * Supports JSON arrays and single JSON objects.
 *
 * @author Maniraj
 */
public final class JsonReader {

    public static final Logger logger = LogManager.getLogger(JsonReader.class);
    private static final Gson GSON = new Gson();

    private JsonReader() {}

    public static <T> List<T> readJsonArray(String filePath, Class<T> clazz) {
        try (FileReader reader = new FileReader(filePath)) {
            Type listType = TypeToken.getParameterized(List.class, clazz).getType();
            List<T> result = GSON.fromJson(reader, listType);
            logger.info("JSON array read: {} items from {}", result.size(), filePath);
            return result;
        } catch (IOException e) {
            logger.error("Failed to read JSON array from: {}", filePath, e);
            throw new RuntimeException("JSON read error: " + e.getMessage(), e);
        }
    }

    public static <T> T readJsonObject(String filePath, Class<T> clazz) {
        try (FileReader reader = new FileReader(filePath)) {
            T result = GSON.fromJson(reader, clazz);
            logger.info("JSON object read from: {}", filePath);
            return result;
        } catch (IOException e) {
            logger.error("Failed to read JSON object from: {}", filePath, e);
            throw new RuntimeException("JSON read error: " + e.getMessage(), e);
        }
    }
}
