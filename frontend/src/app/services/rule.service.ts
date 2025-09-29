import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Rule, GitRepository } from '../models/rule.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RuleService {
  private apiUrl = environment.apiUrl + '/api/rules';

  constructor(private http: HttpClient) {}

  fetchRulesFromGit(gitRepo: GitRepository): Observable<Rule[]> {
    return this.http.post<Rule[]>(`${this.apiUrl}/fetch`, gitRepo);
  }

  getAllRules(): Observable<Rule[]> {
    return this.http.get<Rule[]>(this.apiUrl);
  }

  getRuleById(id: number): Observable<Rule> {
    return this.http.get<Rule>(`${this.apiUrl}/${id}`);
  }

  createRule(rule: Rule): Observable<Rule> {
    return this.http.post<Rule>(this.apiUrl, rule);
  }

  updateRule(id: number, rule: Rule): Observable<Rule> {
    return this.http.put<Rule>(`${this.apiUrl}/${id}`, rule);
  }

  deleteRule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
