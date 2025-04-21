package com.AutoPOC.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Utility class for reading and handling Excel sheets using Apache POI.
 * Commonly used for test data loading and row indexing in automation frameworks.
 */
public class ExcelReaderUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExcelReaderUtil.class);

    /**
     * Fetches a row as key-value pairs where keys are headers.
     * Uses header row at index 1 by default.
     *
     * @param filePath         Path to the Excel file
     * @param sheetName        Name of the sheet
     * @param keyColumnIndex   Index of the key column (zero-based)
     * @param key              Value to match in the key column
     * @return Map of header-value pairs for the matched row
     */
    public static Map<String, String> getRowByKey(String filePath, String sheetName, int keyColumnIndex, String key) {
        return getRowByKey(filePath, sheetName, keyColumnIndex, key, 1);
    }

    /**
     * Fetches a specific row by key match from a given sheet, using provided header row index.
     *
     * @param filePath         Path to Excel file
     * @param sheetName        Sheet to read
     * @param keyColumnIndex   Column to match against (zero-based)
     * @param key              Lookup value
     * @param headerRowIndex   Header row index (zero-based)
     * @return Map of header-value pairs for matched row
     */
    public static Map<String, String> getRowByKey(String filePath, String sheetName, int keyColumnIndex, String key, int headerRowIndex) {
        Map<String, String> rowData = new LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = WorkbookFactory.create(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + sheetName);

            Row header = sheet.getRow(headerRowIndex);
            if (header == null) throw new IllegalArgumentException("Header row missing at index " + headerRowIndex);

            for (int r = headerRowIndex + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String cellVal = getCellValue(row.getCell(keyColumnIndex)).trim();
                if (cellVal.equalsIgnoreCase(key.trim())) {
                    for (int c = 0; c < header.getLastCellNum(); c++) {
                        rowData.put(getCellValue(header.getCell(c)).trim(),
                                getCellValue(row.getCell(c)).trim());
                    }
                    break;
                }
            }

            if (rowData.isEmpty()) {
                throw new RuntimeException("No row found for key: " + key +
                        " in sheet: " + sheetName + " (col " + keyColumnIndex + ")");
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading Excel file: " + filePath, e);
        }

        return rowData;
    }

    /**
     * Loads all rows from a sheet as List<Map> with column header mapping.
     * Header is expected at row index 0.
     *
     * @param filePath   Path to Excel file
     * @param sheetName  Sheet name to read
     * @return List of Maps, each representing one row
     */
    public static List<Map<String, String>> getAllRows(String filePath, String sheetName) {
        List<Map<String, String>> all = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) {
                logger.error("Sheet '{}' not found in {}", sheetName, filePath);
                return all;
            }

            Row header = sheet.getRow(0);
            if (header == null) {
                logger.error("Header row missing in sheet: {}", sheetName);
                return all;
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int c = 0; c < header.getLastCellNum(); c++) {
                    rowMap.put(getCellValue(header.getCell(c)).trim(),
                            getCellValue(row.getCell(c)).trim());
                }
                all.add(rowMap);
            }

        } catch (Exception e) {
            logger.error("Error reading rows from {}", sheetName, e);
        }

        return all;
    }

    /**
     * Returns the first available row index based on blank 'Run ID' (column F).
     * Starts from row index 2 (assuming headers + metadata in row 0 & 1).
     *
     * @param sheet Sheet to check
     * @return Index of the next available row to write
     */
    public static int findNextAvailableRow(Sheet sheet) {
        final int RUN_ID_COL_INDEX = 5; // F column (zero-based)

        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell runIdCell = row.getCell(RUN_ID_COL_INDEX, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (runIdCell == null || runIdCell.toString().trim().isEmpty()) {
                logger.info("Next available row found at index {}", i);
                return i;
            }
        }

        int nextRow = sheet.getLastRowNum() + 1;
        logger.info("All Run ID rows filled. Appending new row at index {}", nextRow);
        return nextRow;
    }

    /**
     * Returns value from any cell as String (supports numeric/date/formula/boolean).
     *
     * @param cell Cell to extract value from
     * @return String version of cell value
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    double d = cell.getNumericCellValue();
                    long l = (long) d;
                    yield (d == l) ? String.valueOf(l) : String.valueOf(d);
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK   -> "";
            default      -> "";
        };
    }

    /**
     * Checks if an entire row is effectively empty (no meaningful cell content).
     *
     * @param row Excel row
     * @return true if empty
     */
    private static boolean rowIsEmpty(Row row) {
        if (row == null) return true;

        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && !getCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Fetches a Sheet instance from file and sheet name.
     *
     * @param filePath   File path of the Excel file
     * @param sheetName  Name of the sheet to load
     * @return Sheet object
     */
    public static Sheet getSheet(String filePath, String sheetName) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            Workbook wb = WorkbookFactory.create(fis);
            return wb.getSheet(sheetName);
        } catch (IOException e) {
            throw new RuntimeException("Unable to get sheet: " + sheetName + " from file: " + filePath, e);
        }
    }
}
