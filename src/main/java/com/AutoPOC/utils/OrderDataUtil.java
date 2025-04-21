package com.AutoPOC.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Utility class to log Order ID and Order Date into Excel for each execution.
 * Fields are recorded under 'Transactional_Data_Sheet_Name'.
 */
public class OrderDataUtil {

    private static final Logger logger = LoggerFactory.getLogger(OrderDataUtil.class);

    private static final String FILE_PATH = ConfigReader.getProperty("Test_Data_File_Path");
    private static final String SHEET_NAME = ConfigReader.getProperty("Transactional_Data_Sheet_Name");

    // Column indexes (zero-based)
    private static final int ORDER_ID_COL = 9;   // Column J
    private static final int ORDER_DATE_COL = 10; // Column K

    /**
     * Writes the given order number and order date into specified row in Excel.
     *
     * @param orderNum  Order ID string
     * @param orderDate Order Date string (formatted)
     * @param rowIndex  Target row index (zero-based)
     */
    public static void writeOrderData(String orderNum, String orderDate, int rowIndex) {
        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheet(SHEET_NAME);
            Row row = sheet.getRow(rowIndex);
            if (row == null) row = sheet.createRow(rowIndex);

            CellStyle style = createBorderStyle(wb);
            setCell(row, ORDER_ID_COL, orderNum, style);
            setCell(row, ORDER_DATE_COL, orderDate, style);

            try (FileOutputStream out = new FileOutputStream(FILE_PATH)) {
                wb.write(out);
            }

            logger.info("Order data written to row {}: [ID={}, Date={}]", rowIndex + 1, orderNum, orderDate);

        } catch (IOException e) {
            logger.error("Failed to write order data", e);
        }
    }

    /**
     * Creates a reusable cell style with borders for formatting.
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
     * Sets a value into a cell with the specified style.
     */
    private static void setCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}