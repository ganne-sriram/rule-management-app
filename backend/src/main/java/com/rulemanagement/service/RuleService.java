package com.rulemanagement.service;

import com.rulemanagement.model.GitRepository;
import com.rulemanagement.model.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RuleService {

    @Autowired
    private GitService gitService;

    @Autowired
    private ExcelService excelService;

    private final Map<Long, Rule> rulesCache = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private GitRepository lastUsedGitRepo;

    public List<Rule> fetchRulesFromGit(GitRepository gitRepo) throws Exception {
        File excelFile = gitService.fetchFileFromGit(gitRepo);
        List<Rule> rules = excelService.parseExcelFile(excelFile);
        
        rulesCache.clear();
        for (Rule rule : rules) {
            rulesCache.put(rule.getId(), rule);
        }
        
        this.lastUsedGitRepo = gitRepo;
        
        return rules;
    }

    public GitRepository getLastUsedGitRepo() {
        return lastUsedGitRepo;
    }

    public List<Rule> getAllRules() {
        return new ArrayList<>(rulesCache.values());
    }

    public Rule getRuleById(Long id) {
        return rulesCache.get(id);
    }

    public Rule createRule(Rule rule) {
        rule.setId(idGenerator.getAndIncrement());
        rulesCache.put(rule.getId(), rule);
        return rule;
    }

    public Rule updateRule(Long id, Rule updatedRule) {
        if (rulesCache.containsKey(id)) {
            updatedRule.setId(id);
            rulesCache.put(id, updatedRule);
            return updatedRule;
        }
        return null;
    }

    public boolean deleteRule(Long id) {
        return rulesCache.remove(id) != null;
    }
}
