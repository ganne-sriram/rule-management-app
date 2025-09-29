package com.rulemanagement.service;

import com.rulemanagement.model.Rule;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
