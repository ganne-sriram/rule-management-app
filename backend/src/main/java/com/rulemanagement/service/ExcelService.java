package com.rulemanagement.service;

import com.rulemanagement.model.Rule;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class ExcelService {

    public List<Rule> parseExcelFile(File excelFile) throws IOException {
        List<Rule> rules = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(excelFile)) {
            Workbook workbook;
            String fileName = excelFile.getName().toLowerCase();
            
            if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            } else {
                throw new IOException("Unsupported file format. Only .xlsx and .xls files are supported.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            List<String> headers = new ArrayList<>();
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                for (Cell cell : headerRow) {
                    headers.add(getCellValueAsString(cell));
                }
            }

            long ruleId = 1;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Rule rule = new Rule();
                rule.setId(ruleId++);

                Map<String, Object> conditions = new HashMap<>();
                Map<String, Object> actions = new HashMap<>();

                for (int i = 0; i < headers.size() && i < row.getLastCellNum(); i++) {
                    String header = headers.get(i);
                    Cell cell = row.getCell(i);
                    String cellValue = getCellValueAsString(cell);

                    if (header.toLowerCase().contains("name") || header.toLowerCase().contains("rule")) {
                        rule.setName(cellValue);
                    } else if (header.toLowerCase().contains("description")) {
                        rule.setDescription(cellValue);
                    } else if (header.toLowerCase().contains("condition") || 
                               header.toLowerCase().contains("when") ||
                               header.toLowerCase().contains("if")) {
                        conditions.put(header, cellValue);
                    } else if (header.toLowerCase().contains("action") || 
                               header.toLowerCase().contains("then") ||
                               header.toLowerCase().contains("do")) {
                        actions.put(header, cellValue);
                    } else {
                        conditions.put(header, cellValue);
                    }
                }

                rule.setConditions(conditions);
                rule.setActions(actions);

                if (rule.getName() == null || rule.getName().trim().isEmpty()) {
                    rule.setName("Rule " + rule.getId());
                }

                rules.add(rule);
            }

            workbook.close();
        }

        return rules;
    }

    public File writeRulesToExcel(List<Rule> rules, File templateFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(templateFile);
             Workbook workbook = templateFile.getName().endsWith(".xlsx") ? 
                 new XSSFWorkbook(fis) : new HSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            int dataStartRow = findDataStartRow(sheet);
            if (dataStartRow == -1) {
                throw new IOException("Could not find data start row. Excel file may not be in Drools Decision Table format.");
            }
            
            for (int i = sheet.getLastRowNum(); i >= dataStartRow; i--) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    sheet.removeRow(row);
                }
            }
            
            List<String> conditionKeys = extractColumnHeaders(sheet, "CONDITION");
            List<String> actionKeys = extractColumnHeaders(sheet, "ACTION");
            
            for (int i = 0; i < rules.size(); i++) {
                Rule rule = rules.get(i);
                Row row = sheet.createRow(dataStartRow + i);
                
                Cell nameCell = row.createCell(0);
                nameCell.setCellValue(rule.getName());
                
                int colIndex = 1;
                for (String key : conditionKeys) {
                    Object value = rule.getConditions().get(key);
                    if (value != null) {
                        Cell cell = row.createCell(colIndex);
                        setCellValue(cell, value);
                    }
                    colIndex++;
                }
                
                for (String key : actionKeys) {
                    Object value = rule.getActions().get(key);
                    if (value != null) {
                        Cell cell = row.createCell(colIndex);
                        setCellValue(cell, value);
                    }
                    colIndex++;
                }
            }
            
            File outputFile = Files.createTempFile("rules-", ".xlsx").toFile();
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
            
            return outputFile;
        }
    }

    private int findDataStartRow(Sheet sheet) {
        for (int i = 0; i <= 20 && i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            Cell cell = row.getCell(0);
            if (cell != null) {
                String value = getCellValueAsString(cell).trim().toUpperCase();
                if (value.equals("CONDITION") || value.equals("ACTION")) {
                    return i + 3;
                }
            }
        }
        return -1;
    }

    private List<String> extractColumnHeaders(Sheet sheet, String headerType) {
        List<String> headers = new ArrayList<>();
        
        for (int i = 0; i <= 20 && i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            Cell firstCell = row.getCell(0);
            if (firstCell != null && getCellValueAsString(firstCell).trim().toUpperCase().equals(headerType)) {
                Row nameRow = sheet.getRow(i + 1);
                if (nameRow != null) {
                    for (int colIndex = 1; colIndex <= nameRow.getLastCellNum(); colIndex++) {
                        Cell nameCell = nameRow.getCell(colIndex);
                        if (nameCell != null) {
                            String headerName = getCellValueAsString(nameCell).trim();
                            if (!headerName.isEmpty()) {
                                headers.add(headerName);
                            }
                        }
                    }
                }
                break;
            }
        }
        
        return headers;
    }

    private void setCellValue(Cell cell, Object value) {
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
