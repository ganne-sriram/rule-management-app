package com.rulemanagement.model;

import java.util.Map;
import java.util.HashMap;

public class Rule {
    private Long id;
    private String name;
    private Map<String, Object> conditions;
    private Map<String, Object> actions;
    private String description;
    private boolean active;

    public Rule() {
        this.conditions = new HashMap<>();
        this.actions = new HashMap<>();
        this.active = true;
    }

    public Rule(Long id, String name, String description) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getConditions() {
        return conditions;
    }

    public void setConditions(Map<String, Object> conditions) {
        this.conditions = conditions;
    }

    public Map<String, Object> getActions() {
        return actions;
    }

    public void setActions(Map<String, Object> actions) {
        this.actions = actions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
