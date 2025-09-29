import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RuleService } from '../../services/rule.service';
import { GitRepository, Rule } from '../../models/rule.model';

@Component({
  selector: 'app-git-config',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './git-config.component.html',
  styleUrls: ['./git-config.component.css']
})
export class GitConfigComponent {
  gitRepo: GitRepository = {
    url: '',
    branch: 'main',
    filePath: '',
    username: '',
    token: ''
  };

  loading = false;
  error = '';
  success = false;

  constructor(private ruleService: RuleService) {}

  onSubmit() {
    if (!this.gitRepo.url || !this.gitRepo.filePath) {
      this.error = 'Please fill in required fields (URL and File Path)';
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = false;

    this.ruleService.fetchRulesFromGit(this.gitRepo).subscribe({
      next: (rules: Rule[]) => {
        this.loading = false;
        this.success = true;
        this.error = '';
        console.log('Rules fetched successfully:', rules);
      },
      error: (err) => {
        this.loading = false;
        this.error = 'Failed to fetch rules: ' + (err.error || err.message);
        this.success = false;
      }
    });
  }
}
