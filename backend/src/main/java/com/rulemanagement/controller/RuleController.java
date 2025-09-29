package com.rulemanagement.controller;

import com.rulemanagement.model.GitRepository;
import com.rulemanagement.model.Rule;
import com.rulemanagement.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin(origins = {"http://localhost:4200", "https://*.vercel.app"})
public class RuleController {

    @Autowired
    private RuleService ruleService;

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
}
