import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RuleService } from '../../services/rule.service';
import { Rule } from '../../models/rule.model';

@Component({
  selector: 'app-rule-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rule-form.component.html',
  styleUrls: ['./rule-form.component.css']
})
export class RuleFormComponent implements OnInit {
  rule: Rule = {
    id: 0,
    name: '',
    conditions: {},
    actions: {},
    description: '',
    active: true
  };

  isEditMode = false;
  loading = false;
  error = '';
  conditionKeys: string[] = [''];
  actionKeys: string[] = [''];

  constructor(
    private ruleService: RuleService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id && id !== 'new') {
      this.isEditMode = true;
      this.loadRule(parseInt(id));
    } else {
      this.addConditionField();
      this.addActionField();
    }
  }

  loadRule(id: number) {
    this.loading = true;
    this.ruleService.getRuleById(id).subscribe({
      next: (rule: Rule) => {
        this.rule = rule;
        this.conditionKeys = Object.keys(rule.conditions);
        this.actionKeys = Object.keys(rule.actions);
        if (this.conditionKeys.length === 0) this.addConditionField();
        if (this.actionKeys.length === 0) this.addActionField();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load rule: ' + (err.error || err.message);
        this.loading = false;
      }
    });
  }

  addConditionField() {
    this.conditionKeys.push('');
  }

  removeConditionField(index: number) {
    if (this.conditionKeys.length > 1) {
      const key = this.conditionKeys[index];
      this.conditionKeys.splice(index, 1);
      if (key && this.rule.conditions[key] !== undefined) {
        delete this.rule.conditions[key];
      }
    }
  }

  addActionField() {
    this.actionKeys.push('');
  }

  removeActionField(index: number) {
    if (this.actionKeys.length > 1) {
      const key = this.actionKeys[index];
      this.actionKeys.splice(index, 1);
      if (key && this.rule.actions[key] !== undefined) {
        delete this.rule.actions[key];
      }
    }
  }

  updateConditionKey(oldKey: string, newKey: string, index: number) {
    if (oldKey && this.rule.conditions[oldKey] !== undefined) {
      const value = this.rule.conditions[oldKey];
      delete this.rule.conditions[oldKey];
      if (newKey) {
        this.rule.conditions[newKey] = value;
      }
    }
    this.conditionKeys[index] = newKey;
  }

  updateActionKey(oldKey: string, newKey: string, index: number) {
    if (oldKey && this.rule.actions[oldKey] !== undefined) {
      const value = this.rule.actions[oldKey];
      delete this.rule.actions[oldKey];
      if (newKey) {
        this.rule.actions[newKey] = value;
      }
    }
    this.actionKeys[index] = newKey;
  }

  onSubmit() {
    if (!this.rule.name.trim()) {
      this.error = 'Rule name is required';
      return;
    }

    const cleanConditions: { [key: string]: any } = {};
    const cleanActions: { [key: string]: any } = {};

    this.conditionKeys.forEach(key => {
      if (key.trim() && this.rule.conditions[key] !== undefined) {
        cleanConditions[key.trim()] = this.rule.conditions[key];
      }
    });

    this.actionKeys.forEach(key => {
      if (key.trim() && this.rule.actions[key] !== undefined) {
        cleanActions[key.trim()] = this.rule.actions[key];
      }
    });

    this.rule.conditions = cleanConditions;
    this.rule.actions = cleanActions;

    this.loading = true;
    this.error = '';

    const operation = this.isEditMode 
      ? this.ruleService.updateRule(this.rule.id, this.rule)
      : this.ruleService.createRule(this.rule);

    operation.subscribe({
      next: (savedRule: Rule) => {
        this.loading = false;
        this.router.navigate(['/rules']);
      },
      error: (err) => {
        this.error = 'Failed to save rule: ' + (err.error || err.message);
        this.loading = false;
      }
    });
  }

  cancel() {
    this.router.navigate(['/rules']);
  }
}
