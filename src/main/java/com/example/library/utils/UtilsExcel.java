package com.example.library.utils;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class UtilsExcel {
    public static boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValue(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((int) cell.getNumericCellValue());
        return "";
    }

    public static <T> void exportToExcel(
            HttpServletResponse response,
            String title,
            String sheetName,
            List<String> headers,
            Map<Integer, String[]> dropdownOptions,
            Map<Integer, Integer> columnWidths,
            List<T> dataRows,
            Function<T, List<Object>> dataMapper,
            BiConsumer<XSSFWorkbook, Cell[]> richHeaderFormatter
    ) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName != null ? sheetName : "Template");
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        Row titleRow = sheet.createRow(1);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.size() - 1));
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setWrapText(true);
        Row headerRow = sheet.createRow(3);
        headerRow.setHeightInPoints(60);
        Cell[] headerCells = new Cell[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            headerCells[i] = headerRow.createCell(i);
            headerCells[i].setCellStyle(headerStyle);
        }
        if (richHeaderFormatter != null) {
            richHeaderFormatter.accept(workbook, headerCells);
        } else {
            for (int i = 0; i < headers.size(); i++) {
                headerCells[i].setCellValue(headers.get(i));
            }
        }
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);
        int rowIndex = 4;
        if (dataRows != null && !dataRows.isEmpty() && dataMapper != null) {
            for (T item : dataRows) {
                List<Object> cellValues = dataMapper.apply(item);
                Row row = sheet.createRow(rowIndex++);
                for (int i = 0; i < cellValues.size(); i++) {
                    Cell cell = row.createCell(i);
                    Object value = cellValues.get(i);
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue("");
                    }
                    cell.setCellStyle(dataStyle);
                }
            }
        }
        if (columnWidths != null) {
            for (int i = 0; i < headers.size(); i++) {
                sheet.setColumnWidth(i, columnWidths.getOrDefault(i, 6000));
            }
        }
        if (dropdownOptions != null && !dropdownOptions.isEmpty()) {
            DataValidationHelper validationHelper = sheet.getDataValidationHelper();
            int lastRow = Math.max(rowIndex, 100);
            for (Map.Entry<Integer, String[]> entry : dropdownOptions.entrySet()) {
                int colIndex = entry.getKey();
                String[] options = entry.getValue();
                CellRangeAddressList addressList = new CellRangeAddressList(4, lastRow, colIndex, colIndex);
                DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(options);
                DataValidation validation = validationHelper.createValidation(constraint, addressList);
                if (validation instanceof XSSFDataValidation) {
                    validation.setSuppressDropDownArrow(true);
                    validation.setShowErrorBox(true);
                }
                sheet.addValidationData(validation);
            }
        }
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
