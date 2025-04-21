package com.AutoPOC.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class to record execution metadata into an Excel sheet.
 * Records include Run ID, Execution Date, Time, and Status.
 */
public class ExecutionDataUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionDataUtil.class);

    private static final String FILE_PATH = ConfigReader.getProperty("Test_Data_File_Path");
    private static final String SHEET_NAME = ConfigReader.getProperty("Transactional_Data_Sheet_Name");

    // Column indexes (zero-based)
    private static final int RUN_ID_COL = 5; // Column F
    private static final int EXEC_DATE_COL = 6; // Column G
    private static final int EXEC_TIME_COL = 7; // Column H
    private static final int EXEC_STATUS_COL = 8; // Column I

    /**
     * Writes execution metadata to the specified row of the sheet.
     *
     * @param rowIndex Target row index to write data
     * @param result   ITestResult containing execution status
     */
    public static void writeExecutionData(int rowIndex, ITestResult result) {
        String execDate = getCurrentDate("MM/dd/yyyy");
        String execTime = getCurrentDate("HH:mm:ss");
        String status = getStatus(result);

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(SHEET_NAME);
            if (sheet == null) return;

            Row row = sheet.getRow(rowIndex);
            if (row == null) row = sheet.createRow(rowIndex);

            String runId = "R" + (countExistingRunIds(sheet) + 1);
            CellStyle style = createBorderStyle(wb);

            setCell(row, RUN_ID_COL, runId, style);
            setCell(row, EXEC_DATE_COL, execDate, style);
            setCell(row, EXEC_TIME_COL, execTime, style);
            setCell(row, EXEC_STATUS_COL, status, style);

            try (FileOutputStream out = new FileOutputStream(FILE_PATH)) {
                wb.write(out);
            }

            logger.info("Execution data written to row {}: RunID={}, Status={}", rowIndex + 1, runId, status);

        } catch (IOException e) {
            logger.error("Failed to write execution data", e);
        }
    }

    /**
     * Counts the number of non-empty Run ID cells in the sheet.
     */
    private static int countExistingRunIds(Sheet sheet) {
        int count = 0;
        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(RUN_ID_COL, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null && !cell.toString().trim().isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Gets the execution status as string based on result status code.
     */
    private static String getStatus(ITestResult result) {
        return switch (result.getStatus()) {
            case ITestResult.SUCCESS -> "Pass";
            case ITestResult.FAILURE -> "Fail";
            case ITestResult.SKIP -> "Skipped";
            default -> "Unknown";
        };
    }

    /**
     * Formats and returns current date/time.
     */
    private static String getCurrentDate(String pattern) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Creates a reusable cell style with borders.
     */
    private static CellStyle createBorderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Writes a value to a cell with the given style.
     */
    private static void setCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}