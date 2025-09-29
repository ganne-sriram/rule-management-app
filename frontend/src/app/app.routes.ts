import { Routes } from '@angular/router';
import { GitConfigComponent } from './components/git-config/git-config.component';
import { RuleListComponent } from './components/rule-list/rule-list.component';
import { RuleFormComponent } from './components/rule-form/rule-form.component';

export const routes: Routes = [
  { path: '', redirectTo: '/git-config', pathMatch: 'full' },
  { path: 'git-config', component: GitConfigComponent },
  { path: 'rules', component: RuleListComponent },
  { path: 'rule/new', component: RuleFormComponent },
  { path: 'rule/:id', component: RuleFormComponent }
];
