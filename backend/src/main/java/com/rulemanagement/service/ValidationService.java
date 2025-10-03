package com.rulemanagement.service;

import com.rulemanagement.model.ValidationResult;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValidationService {

    public ValidationResult validateDroolsFormat(File excelFile) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(excelFile)) {
            SpreadsheetCompiler compiler = new SpreadsheetCompiler();
            String drl = compiler.compile(fis, "xls");

            if (drl == null || drl.isEmpty()) {
                errors.add("Failed to compile Excel file to Drools DRL");
            } else if (!drl.contains("rule ")) {
                warnings.add("DRL does not contain any rule definitions");
            }

        } catch (Exception e) {
            errors.add("Drools validation failed: " + e.getMessage());
        }

        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
}
