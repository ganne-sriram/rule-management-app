import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RuleService } from '../../services/rule.service';
import { Rule, GitRepository } from '../../models/rule.model';

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
  lastSavedBranch: string | null = null;
  gitRepoConfig: GitRepository | null = null;

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

  saveToGit() {
    const urlInput = prompt('Enter Git repository URL:');
    if (!urlInput) return;

    const branchInput = prompt('Enter base branch (default: main):', 'main');
    const filePathInput = prompt('Enter file path in repository (e.g., rules/EligibilityRules.xlsx):');
    if (!filePathInput) return;

    const usernameInput = prompt('Enter Git username:');
    const tokenInput = prompt('Enter Git token (PAT):');

    if (!usernameInput || !tokenInput) {
      this.error = 'Username and token are required';
      return;
    }

    this.gitRepoConfig = {
      url: urlInput,
      branch: branchInput || 'main',
      filePath: filePathInput,
      username: usernameInput,
      token: tokenInput
    };

    this.loading = true;
    this.error = '';

    const request = {
      gitRepo: this.gitRepoConfig,
      commitMessage: 'Update decision table rules from web interface'
    };

    this.ruleService.saveRulesToGit(request).subscribe({
      next: (response) => {
        this.lastSavedBranch = response.branch;
        let message = `Successfully saved to Git on branch: ${response.branch}`;
        if (response.warnings && response.warnings.length > 0) {
          message += '\n\nWarnings:\n' + response.warnings.join('\n');
        }
        alert(message);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to save to Git: ' + (err.error?.message || err.error || err.message);
        this.loading = false;
      }
    });
  }

  createPR() {
    if (!this.lastSavedBranch) {
      this.error = 'Please save to Git first before creating a PR';
      return;
    }

    if (!this.gitRepoConfig) {
      this.error = 'Git repository configuration not found';
      return;
    }

    const titleInput = prompt('Enter PR title:', 'Update decision table rules');
    if (!titleInput) return;

    const descriptionInput = prompt('Enter PR description (optional):', 'Updated rules via web interface');

    this.loading = true;
    this.error = '';

    const request = {
      gitRepo: this.gitRepoConfig,
      branchName: this.lastSavedBranch,
      title: titleInput,
      description: descriptionInput || 'Updated rules via web interface'
    };

    this.ruleService.createPullRequest(request).subscribe({
      next: (response) => {
        alert('Pull request created successfully!\n\n' + response.prUrl);
        window.open(response.prUrl, '_blank');
        this.loading = false;
        this.lastSavedBranch = null;
      },
      error: (err) => {
        this.error = 'Failed to create PR: ' + (err.error?.message || err.error || err.message);
        this.loading = false;
      }
    });
  }
}
