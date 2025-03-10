package com.AutoPOC.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static final Properties properties = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("src/test/resources/config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties file", e);
        }
    }

    /**
     * Retrieves a property value. If the key is missing, it returns an empty string.
     *
     * @param key Property key to fetch.
     * @return Property value or empty string if key is missing.
     */
    public static String getProperty(String key) {
        return properties.getProperty(key, "").trim();
    }

    /**
     * Retrieves a property with a fallback default value.
     *
     * @param key          Property key to fetch.
     * @param defaultValue Default value if key is missing.
     * @return Property value or default value.
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue).trim();
    }
}
