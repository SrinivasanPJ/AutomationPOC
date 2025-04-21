package com.AutoPOC.utils;

import java.util.Map;

/**
 * Thread-safe context manager to hold test input data during execution.
 * Allows test classes to access the synthetic input data mapped by headers.
 */
public class TestContextManager {

    private static final ThreadLocal<Map<String, String>> inputData = new ThreadLocal<>();

    /**
     * Sets the input data for the current thread.
     *
     * @param data Map of column headers to values for test input
     */
    public static void setInputData(Map<String, String> data) {
        inputData.set(data);
    }

    /**
     * Retrieves the input data for the current thread.
     *
     * @return Map of column headers to values
     */
    public static Map<String, String> getInputData() {
        return inputData.get();
    }

    /**
     * Clears the input data for the current thread.
     */
    public static void clear() {
        inputData.remove();
    }
}
