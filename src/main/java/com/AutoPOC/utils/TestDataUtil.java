package com.AutoPOC.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to read test data from Excel files.
 */
public class TestDataUtil {
    private static final Logger logger = LoggerFactory.getLogger(TestDataUtil.class);

    // Fetch test data file path from config
    private static final String TEST_DATA_FILE = ConfigReader.getProperty("Test_Data_File_Path");

    /**
     * Retrieves login details from an Excel file.
     *
     * @param filePath  The path to the Excel file.
     * @param sheetName The name of the sheet containing login data.
     * @return A 2D array containing login details.
     */
    public static Object[][] getLoginDetails(String filePath, String sheetName) {
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(new File(filePath)))) {
            Sheet sheet = workbook.getSheet(sheetName);
            int totalRows = sheet.getPhysicalNumberOfRows();
            int totalColumns = sheet.getRow(0).getLastCellNum();
            Object[][] data = new Object[totalRows - 1][totalColumns];

            for (int i = 1; i < totalRows; i++) { // Skip header row
                Row row = sheet.getRow(i);
                for (int j = 0; j < totalColumns; j++) {
                    data[i - 1][j] = getCellValue(row.getCell(j));
                }
            }
            return data;
        } catch (IOException e) {
            logger.error("Error reading login details from file: {}", filePath, e);
            return new Object[0][0]; // Return empty array if error occurs
        }
    }

    /**
     * Retrieves test case data for a specific Test ID.
     *
     * @param sheetName The name of the sheet.
     * @param testID    The Test ID to search for.
     * @return A map containing test data for the given Test ID, or null if not found.
     */
    public static Map<String, String> getTestCaseByTestID(String sheetName, String testID) {
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(new File(TEST_DATA_FILE)))) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                logger.error("Sheet '{}' not found in file: {}", sheetName, TEST_DATA_FILE);
                return null;
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String currentTestID = getCellValue(row.getCell(0));
                if (currentTestID.equals(testID)) {
                    Map<String, String> testData = new HashMap<>();
                    testData.put("TestID", currentTestID);
                    testData.put("URL", getCellValue(row.getCell(1)));
                    testData.put("username", getCellValue(row.getCell(2)));
                    testData.put("password", getCellValue(row.getCell(3)));
                    testData.put("browser", getCellValue(row.getCell(4)));
                    return testData;
                }
            }
            logger.warn("TestID '{}' not found in sheet '{}'", testID, sheetName);
        } catch (IOException e) {
            logger.error("Error reading test data from file: {}", TEST_DATA_FILE, e);
        }
        return null; // Return null if TestID not found
    }

    /**
     * Retrieves the URL from the first row, first column of a given sheet.
     *
     * @param filePath  The path to the Excel file.
     * @param sheetName The name of the sheet containing the URL.
     * @return The URL as a string, or an empty string if an error occurs.
     */
    public static String getURL(String filePath, String sheetName) {
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(new File(filePath)))) {
            return getCellValue(workbook.getSheet(sheetName).getRow(1).getCell(0));
        } catch (IOException e) {
            logger.error("Error reading URL from file: {}", filePath, e);
            return "";
        }
    }

    /**
     * Safely retrieves the value of a cell, handling different data types.
     *
     * @param cell The cell to retrieve data from.
     * @return A string representation of the cell's value.
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString(); // Convert date to string
                }
                return String.valueOf((int) cell.getNumericCellValue()); // Convert number to string (remove .0)
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
