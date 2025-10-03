package com.rulemanagement.controller;

import com.rulemanagement.model.GitRepository;
import com.rulemanagement.model.PullRequestRequest;
import com.rulemanagement.model.Rule;
import com.rulemanagement.model.ValidationResult;
import com.rulemanagement.service.ExcelService;
import com.rulemanagement.service.GitService;
import com.rulemanagement.service.RuleService;
import com.rulemanagement.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin(origins = {"http://localhost:4200", "https://*.vercel.app"})
public class RuleController {

    @Autowired
    private RuleService ruleService;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private GitService gitService;

    @Autowired
    private ValidationService validationService;

    @PostMapping("/fetch")
    public ResponseEntity<?> fetchRulesFromGit(@RequestBody GitRepository gitRepo) {
        try {
            List<Rule> rules = ruleService.fetchRulesFromGit(gitRepo);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching rules from Git: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Rule>> getAllRules() {
        List<Rule> rules = ruleService.getAllRules();
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rule> getRuleById(@PathVariable Long id) {
        Rule rule = ruleService.getRuleById(id);
        if (rule != null) {
            return ResponseEntity.ok(rule);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Rule> createRule(@RequestBody Rule rule) {
        Rule createdRule = ruleService.createRule(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rule> updateRule(@PathVariable Long id, @RequestBody Rule rule) {
        Rule updatedRule = ruleService.updateRule(id, rule);
        if (updatedRule != null) {
            return ResponseEntity.ok(updatedRule);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        boolean deleted = ruleService.deleteRule(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/save-to-git")
    public ResponseEntity<?> saveRulesToGit(@RequestBody PullRequestRequest request) {
        try {
            List<Rule> rules = ruleService.getAllRules();
            
            if (rules.isEmpty()) {
                return ResponseEntity.badRequest().body("No rules to save");
            }
            
            File templateFile = gitService.fetchFileFromGit(request.getGitRepo());
            
            File updatedExcel = excelService.writeRulesToExcel(rules, templateFile);
            
            ValidationResult validation = validationService.validateDroolsFormat(updatedExcel);
            if (!validation.isValid()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("valid", false);
                errorResponse.put("errors", validation.getErrors());
                errorResponse.put("warnings", validation.getWarnings());
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            long timestamp = System.currentTimeMillis() / 1000;
            String branchName = request.getBranchName() != null ? 
                request.getBranchName() : 
                "devin/" + timestamp + "-rules-update";
            
            String pushedBranch = gitService.createBranchAndPush(
                request.getGitRepo(), 
                updatedExcel, 
                branchName,
                request.getCommitMessage() != null ? 
                    request.getCommitMessage() : 
                    "Update decision table rules"
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully pushed to branch");
            response.put("branch", pushedBranch);
            response.put("warnings", validation.getWarnings());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error saving to Git: " + e.getMessage());
        }
    }

    @PostMapping("/create-pr")
    public ResponseEntity<?> createPullRequest(@RequestBody PullRequestRequest request) {
        try {
            if (request.getBranchName() == null || request.getBranchName().isEmpty()) {
                return ResponseEntity.badRequest().body("Branch name is required");
            }
            
            if (request.getTitle() == null || request.getTitle().isEmpty()) {
                return ResponseEntity.badRequest().body("PR title is required");
            }
            
            String prUrl = gitService.createPullRequest(
                request.getGitRepo(),
                request.getBranchName(),
                request.getTitle(),
                request.getDescription() != null ? request.getDescription() : ""
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Pull request created successfully");
            response.put("prUrl", prUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating PR: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateRules() {
        try {
            List<Rule> rules = ruleService.getAllRules();
            
            if (rules.isEmpty()) {
                return ResponseEntity.badRequest().body("No rules to validate");
            }
            
            GitRepository tempGitRepo = ruleService.getLastUsedGitRepo();
            if (tempGitRepo == null) {
                return ResponseEntity.badRequest()
                    .body("No Git repository configured. Please fetch rules from Git first.");
            }
            
            File templateFile = gitService.fetchFileFromGit(tempGitRepo);
            File excelFile = excelService.writeRulesToExcel(rules, templateFile);
            
            ValidationResult result = validationService.validateDroolsFormat(excelFile);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Validation error: " + e.getMessage());
        }
    }
}
