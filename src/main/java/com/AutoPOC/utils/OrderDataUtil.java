package com.AutoPOC.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class OrderDataUtil {
    private static final Logger logger = LoggerFactory.getLogger(OrderDataUtil.class);
    private static final String[] HEADERS = {"Order ID", "Order Date", "Order Status"};

    /**
     * Writes order data to the specified Excel file and sheet.
     *
     * @param filePath    The path to the Excel file.
     * @param sheetName   The name of the sheet where the data will be written.
     * @param orderNum    The order ID to be recorded.
     * @param orderDate   The order date to be recorded.
     * @param orderStatus The order status to be recorded.
     */
    public static void writeOrderData(String filePath, String sheetName, String orderNum, String orderDate, String orderStatus) {
        if (filePath == null || filePath.trim().isEmpty()) {
            logger.error("Invalid file path provided.");
            return;
        }

        File file = new File(filePath);
        Workbook workbook;
        boolean isNewFile = !file.exists();

        try {
            if (isNewFile) {
                workbook = new XSSFWorkbook(); // Create new workbook if file doesn't exist
                logger.info("Creating new Excel file: {}", filePath);
            } else {
                try (FileInputStream fis = new FileInputStream(filePath)) {
                    workbook = new XSSFWorkbook(fis);
                }
            }

            Sheet sheet = getOrCreateSheet(workbook, sheetName);
            appendOrderData(sheet, orderNum, orderDate, orderStatus);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

            logger.info("Order saved: ID={}, Date={}, Status={} in {}", orderNum, orderDate, orderStatus, filePath);
            openExcelFile(filePath);
        } catch (IOException e) {
            logger.error("Error writing to file: {}", filePath, e);
        }
    }

    /**
     * Retrieves an existing sheet or creates a new one if it doesn't exist.
     *
     * @param workbook  The workbook instance.
     * @param sheetName The name of the sheet to retrieve or create.
     * @return The sheet instance.
     */
    private static Sheet getOrCreateSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
            createHeaderRow(sheet);
            logger.info("Created new sheet '{}'", sheetName);
        }
        return sheet;
    }

    /**
     * Creates a header row in the given sheet.
     *
     * @param sheet The sheet where the header row should be created.
     */
    private static void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(getHeaderStyle(sheet.getWorkbook()));
        }
    }

    /**
     * Appends a new row with order details to the given sheet.
     *
     * @param sheet       The sheet to append data to.
     * @param orderNum    The order ID.
     * @param orderDate   The order date.
     * @param orderStatus The order status.
     */
    private static void appendOrderData(Sheet sheet, String orderNum, String orderDate, String orderStatus) {
        int lastRowNum = sheet.getLastRowNum();
        Row row = sheet.createRow(lastRowNum + 1);
        row.createCell(0).setCellValue(orderNum);
        row.createCell(1).setCellValue(orderDate);
        row.createCell(2).setCellValue(orderStatus);
    }

    /**
     * Creates a bold header style for the Excel sheet.
     *
     * @param workbook The workbook instance.
     * @return A bold cell style.
     */
    private static CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    /**
     * Opens the specified Excel file in the system's default application.
     *
     * @param filePath The path to the Excel file to be opened.
     */
    private static void openExcelFile(String filePath) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(filePath));
                logger.info("Opened Excel file: {}", filePath);
            } catch (IOException e) {
                logger.error("Failed to open file: {}", filePath, e);
            }
        } else {
            logger.warn("Desktop not supported. Cannot open file.");
        }
    }
}
