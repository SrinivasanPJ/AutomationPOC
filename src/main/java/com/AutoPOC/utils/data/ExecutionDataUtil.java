package com.AutoPOC.utils.data;

import com.AutoPOC.config.ConfigReader;
import com.AutoPOC.utils.reporting.ExtentReportManager;
import com.AutoPOC.utils.reporting.LogUtil;
import com.AutoPOC.utils.excel.ExcelColumnIndex;
import com.AutoPOC.utils.excel.ExcelUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.ITestResult;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class to record execution metadata into an Excel file.
 * It logs details such as Run ID, execution date, execution time,
 * test result status (Pass/Fail/Skip), and failure reason (if applicable).
 */
public class ExecutionDataUtil {

    private static final String FILE_PATH = ConfigReader.getProperty("Test_Data_File_Path");
    private static final String SHEET_NAME = ConfigReader.getProperty("Transactional_Data_Sheet_Name");

    /**
     * Writes the execution metadata for a test into the specified row in the Excel file.
     * It records Run ID, execution date and time, test result status,
     * and the failure reason if the test failed.
     *
     * @param rowIndex The row number in the Excel sheet where data will be written.
     * @param result   The TestNG test result object containing execution status and exception (if any).
     */
    public static void writeExecutionData(int rowIndex, ITestResult result) {
        String execDate = getCurrentDate("MM/dd/yyyy");   // Get today's date
        String execTime = getCurrentDate("HH:mm:ss");     // Get current time
        String status   = getStatus(result);              // Map TestNG status to readable string

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook wb       = new XSSFWorkbook(fis)) {

            // Access the desired sheet
            Sheet sheet = wb.getSheet(SHEET_NAME);
            if (sheet == null) return;

            // Create or retrieve the target row
            Row row = sheet.getRow(rowIndex);
            if (row == null) row = sheet.createRow(rowIndex);

            // Generate the Run ID based on the number of existing runs
            String runId = "R" + (countExistingRunIds(sheet) + 1);

            // Create a consistent cell style with borders
            CellStyle style = createBorderStyle(wb);

            // Write test execution metadata to respective columns
            ExcelUtil.setCellValue(row, ExcelColumnIndex.RUN_ID,      runId,    style);
            ExcelUtil.setCellValue(row, ExcelColumnIndex.EXEC_DATE,   execDate, style);
            ExcelUtil.setCellValue(row, ExcelColumnIndex.EXEC_TIME,   execTime, style);
            ExcelUtil.setCellValue(row, ExcelColumnIndex.EXEC_STATUS, status,   style);

            // ─── Write the failure reason (only if the test failed) ──────────────
            if ("Fail".equalsIgnoreCase(status)) {
                Throwable t = result.getThrowable();

                // Extract the first line of the exception message
                String fullMsg = t != null && t.getMessage() != null
                        ? t.getMessage().split("\\r?\\n")[0]
                        : "No exception message";

                // Truncate message to 100 characters max
                String shortMsg = fullMsg.length() > 100
                        ? fullMsg.substring(0, 100) + "..."
                        : fullMsg;

                // Create a style with borders and text wrapping
                CellStyle wrap = createBorderStyle(wb);
                wrap.setWrapText(true);

                // Widen the failure reason column for readability
                sheet.setColumnWidth(ExcelColumnIndex.FAILURE_REASON, 50 * 256);

                // Write the short failure reason into the corresponding column
                ExcelUtil.setCellValue(
                        row,
                        ExcelColumnIndex.FAILURE_REASON,
                        shortMsg,
                        wrap
                );
            }

            // Save the workbook back to the file
            try (FileOutputStream out = new FileOutputStream(FILE_PATH)) {
                wb.write(out);
            }

            // Log to Extent Report that execution data was written
            ExtentReportManager.logInfoSimple(
                    "Execution data written to Excel: RunID=" + runId +
                            ", Status=" + status
            );

        } catch (IOException e) {
            LogUtil.error(ExecutionDataUtil.class, "Failed to write execution data", e);
        }
    }

    /**
     * Counts the number of existing test runs in the Excel sheet.
     * This is determined by counting non-empty Run ID cells, starting from row index 2.
     *
     * @param sheet The Excel sheet object.
     * @return Total number of non-empty Run ID cells.
     */
    private static int countExistingRunIds(Sheet sheet) {
        int count = 0;
        for (int i = 2; i <= sheet.getLastRowNum(); i++) { // Skip header rows
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(ExcelColumnIndex.RUN_ID, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null && !cell.toString().trim().isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Converts TestNG status codes into human-readable strings.
     *
     * @param result The TestNG test result object.
     * @return "Pass", "Fail", "Skipped", or "Unknown".
     */
    private static String getStatus(ITestResult result) {
        return switch (result.getStatus()) {
            case ITestResult.SUCCESS -> "Pass";
            case ITestResult.FAILURE -> "Fail";
            case ITestResult.SKIP    -> "Skipped";
            default                  -> "Unknown";
        };
    }

    /**
     * Returns the current system date/time in the given format.
     *
     * @param pattern Date/time format (e.g., "MM/dd/yyyy", "HH:mm:ss").
     * @return A string representing the formatted current date/time.
     */
    private static String getCurrentDate(String pattern) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Creates a bordered cell style to visually distinguish written data in Excel.
     *
     * @param wb Workbook object used to create the style.
     * @return A CellStyle with thin borders on all sides.
     */
    private static CellStyle createBorderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
