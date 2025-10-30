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
            List<ExcelSheetConfig<?>> sheetsConfig
    ) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        for (ExcelSheetConfig<?> config : sheetsConfig) {
            Sheet sheet = workbook.createSheet(config.getSheetName() != null ? config.getSheetName()  : "Template");
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
            titleCell.setCellValue(config.getTitle());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, config.getHeaders().size() - 1));
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
            Cell[] headerCells = new Cell[config.getHeaders().size()];
            for (int i = 0; i < config.getHeaders().size(); i++) {
                headerCells[i] = headerRow.createCell(i);
                headerCells[i].setCellStyle(headerStyle);
            }
            if (config.getRichHeaderFormatter() != null) {
                config.getRichHeaderFormatter().accept(workbook, headerCells);
            } else {
                for (int i = 0; i < config.getHeaders().size(); i++) {
                    headerCells[i].setCellValue(config.getHeaders().get(i));
                }
            }
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setWrapText(true);
            int rowIndex = 4;
            if (config.getDataRows() != null && !config.getDataRows().isEmpty() && config.getDataMapper() != null) {
                @SuppressWarnings("unchecked")
                List<Object> dataRows = (List<Object>) (List<?>) config.getDataRows();
                @SuppressWarnings("unchecked")
                Function<Object, List<Object>> mapper = (Function<Object, List<Object>>) (Function<?, List<Object>>) config.getDataMapper();
                for (Object item : dataRows) {
                    List<Object> cellValues = mapper.apply(item);
                    Row row = sheet.createRow(rowIndex++);
                    for (int i = 0; i < cellValues.size(); i++) {
                        Cell cell = row.createCell(i);
                        Object value = cellValues.get(i);
                        cell.setCellValue(value != null ? value.toString() : "");
                        cell.setCellStyle(dataStyle);
                    }
                }
            }
            if (config.getColumnWidths() != null) {
                for (int i = 0; i < config.getHeaders().size(); i++) {
                    sheet.setColumnWidth(i, config.getColumnWidths().getOrDefault(i, 6000));
                }
            }
            if (config.getDropdownOptions() != null && !config.getDropdownOptions().isEmpty()) {
                DataValidationHelper validationHelper = sheet.getDataValidationHelper();
                int lastRow = Math.max(rowIndex, 100);
                for (Map.Entry<Integer, String[]> entry : config.getDropdownOptions().entrySet()) {
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
        }
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public static class ExcelSheetConfig<T> {
        String title;
        String sheetName;
        List<String> headers;
        Map<Integer, String[]> dropdownOptions;
        Map<Integer, Integer> columnWidths;
        List<T> dataRows;
        Function<T, List<Object>> dataMapper;
        BiConsumer<XSSFWorkbook, Cell[]> richHeaderFormatter;

        public ExcelSheetConfig() {
        }

        public ExcelSheetConfig(String title, String sheetName, List<String> headers, Map<Integer, String[]> dropdownOptions, Map<Integer, Integer> columnWidths, List<T> dataRows, Function<T, List<Object>> dataMapper, BiConsumer<XSSFWorkbook, Cell[]> richHeaderFormatter) {
            this.title = title;
            this.sheetName = sheetName;
            this.headers = headers;
            this.dropdownOptions = dropdownOptions;
            this.columnWidths = columnWidths;
            this.dataRows = dataRows;
            this.dataMapper = dataMapper;
            this.richHeaderFormatter = richHeaderFormatter;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSheetName() {
            return sheetName;
        }

        public void setSheetName(String sheetName) {
            this.sheetName = sheetName;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public void setHeaders(List<String> headers) {
            this.headers = headers;
        }

        public Map<Integer, String[]> getDropdownOptions() {
            return dropdownOptions;
        }

        public void setDropdownOptions(Map<Integer, String[]> dropdownOptions) {
            this.dropdownOptions = dropdownOptions;
        }

        public Map<Integer, Integer> getColumnWidths() {
            return columnWidths;
        }

        public void setColumnWidths(Map<Integer, Integer> columnWidths) {
            this.columnWidths = columnWidths;
        }

        public List<T> getDataRows() {
            return dataRows;
        }

        public void setDataRows(List<T> dataRows) {
            this.dataRows = dataRows;
        }

        public Function<T, List<Object>> getDataMapper() {
            return dataMapper;
        }

        public void setDataMapper(Function<T, List<Object>> dataMapper) {
            this.dataMapper = dataMapper;
        }

        public BiConsumer<XSSFWorkbook, Cell[]> getRichHeaderFormatter() {
            return richHeaderFormatter;
        }

        public void setRichHeaderFormatter(BiConsumer<XSSFWorkbook, Cell[]> richHeaderFormatter) {
            this.richHeaderFormatter = richHeaderFormatter;
        }
    }
}
