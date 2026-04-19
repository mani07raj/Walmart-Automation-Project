package com.walmart.framework.utils;

import io.cucumber.datatable.DataTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * ExcelReader — Reads test data from .xlsx files using Apache POI.
 * Supports Builder-style configuration, Map-based row access, and Cucumber DataTable conversion.
 *
 * @author Maniraj
 */
public class ExcelReader {

    public static final Logger logger = LogManager.getLogger(ExcelReader.class);

    private final String fileLocation;
    private final String sheetName;
    private XSSFWorkbook book;

    private ExcelReader(Builder builder) {
        this.fileLocation = builder.fileLocation;
        this.sheetName = builder.sheetName;
    }

    public static class Builder {
        private String fileLocation;
        private String sheetName;

        public Builder setFileLocation(String fileLocation) {
            this.fileLocation = fileLocation;
            return this;
        }

        public Builder setSheet(String sheetName) {
            this.sheetName = sheetName;
            return this;
        }

        public ExcelReader build() {
            Objects.requireNonNull(fileLocation, "fileLocation must not be null");
            Objects.requireNonNull(sheetName, "sheetName must not be null");
            return new ExcelReader(this);
        }
    }

    public List<List<String>> getSheetDataAt() throws IOException {
        List<List<String>> outerList = new LinkedList<>();
        try {
            book = new XSSFWorkbook(new File(fileLocation));
            XSSFSheet sheet = book.getSheet(sheetName);
            if (sheet == null) throw new RuntimeException("Sheet not found: " + sheetName);
            outerList = getSheetData(sheet);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (book != null) book.close();
        }
        return outerList;
    }

    private List<List<String>> getSheetData(XSSFSheet sheet) {
        List<List<String>> outerList = new LinkedList<>();
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            List<String> innerList = new LinkedList<>();
            XSSFRow row = sheet.getRow(i);
            if (row == null) continue;
            for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                innerList.add(getCellValue(row.getCell(j)));
            }
            outerList.add(Collections.unmodifiableList(innerList));
        }
        return Collections.unmodifiableList(outerList);
    }

    /**
     * Reads all data rows from the specified sheet.
     * Each row is returned as a Map of column header to cell value.
     */
    public static List<Map<String, String>> readData(String filePath, String sheetName) throws InvalidFormatException {
        List<Map<String, String>> data = new ArrayList<>();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new File(filePath))) {
            XSSFSheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + sheetName);
            XSSFRow headerRow = sheet.getRow(0);
            if (headerRow == null) return data;
            int colCount = headerRow.getLastCellNum();
            List<String> headers = new ArrayList<>();
            for (int c = 0; c < colCount; c++) {
                headers.add(getCellValue(headerRow.getCell(c)));
            }
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                XSSFRow row = sheet.getRow(r);
                if (row == null) continue;
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int c = 0; c < colCount; c++) {
                    rowMap.put(headers.get(c), getCellValue(row.getCell(c)));
                }
                data.add(rowMap);
            }
            logger.info("Excel read: {} rows from sheet '{}'", data.size(), sheetName);
        } catch (IOException e) {
            logger.error("Failed to read Excel: {}", filePath, e);
            throw new RuntimeException("Excel read error: " + e.getMessage(), e);
        }
        return data;
    }

    /**
     * Reads the specified sheet and converts it to a Cucumber DataTable.
     */
    public static DataTable toDataTable(String filePath, String sheetNameParam) {
        ExcelReader reader = new ExcelReader.Builder()
                .setFileLocation(filePath)
                .setSheet(sheetNameParam)
                .build();
        try {
            List<List<String>> excelData = reader.getSheetDataAt();
            return DataTable.create(excelData);
        } catch (Exception e) {
            throw new RuntimeException("DataTable conversion error: " + e.getMessage(), e);
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case BLANK   -> "";
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                yield (val == Math.floor(val)) ? String.valueOf((long) val) : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> throw new IllegalArgumentException("Cannot read column type: " + cell.getCellType());
        };
    }
}
