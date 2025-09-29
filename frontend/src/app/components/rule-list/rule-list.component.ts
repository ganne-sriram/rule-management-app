import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RuleService } from '../../services/rule.service';
import { Rule } from '../../models/rule.model';

@Component({
  selector: 'app-rule-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './rule-list.component.html',
  styleUrls: ['./rule-list.component.css']
})
export class RuleListComponent implements OnInit {
  rules: Rule[] = [];
  loading = false;
  error = '';
  selectedRule: Rule | null = null;

  constructor(private ruleService: RuleService) {}

  ngOnInit() {
    this.loadRules();
  }

  loadRules() {
    this.loading = true;
    this.error = '';

    this.ruleService.getAllRules().subscribe({
      next: (rules: Rule[]) => {
        this.rules = rules;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load rules: ' + (err.error || err.message);
        this.loading = false;
      }
    });
  }

  selectRule(rule: Rule) {
    this.selectedRule = rule;
  }

  deleteRule(rule: Rule) {
    if (confirm(`Are you sure you want to delete rule "${rule.name}"?`)) {
      this.ruleService.deleteRule(rule.id).subscribe({
        next: () => {
          this.rules = this.rules.filter(r => r.id !== rule.id);
          if (this.selectedRule?.id === rule.id) {
            this.selectedRule = null;
          }
        },
        error: (err) => {
          this.error = 'Failed to delete rule: ' + (err.error || err.message);
        }
      });
    }
  }

  getConditionKeys(): string[] {
    if (this.rules.length === 0) return [];
    const allKeys = new Set<string>();
    this.rules.forEach(rule => {
      Object.keys(rule.conditions || {}).forEach(key => allKeys.add(key));
    });
    return Array.from(allKeys);
  }

  getActionKeys(): string[] {
    if (this.rules.length === 0) return [];
    const allKeys = new Set<string>();
    this.rules.forEach(rule => {
      Object.keys(rule.actions || {}).forEach(key => allKeys.add(key));
    });
    return Array.from(allKeys);
  }

  hasConditions(rule: Rule): boolean {
    return Object.keys(rule.conditions || {}).length > 0;
  }

  hasActions(rule: Rule): boolean {
    return Object.keys(rule.actions || {}).length > 0;
  }
}
