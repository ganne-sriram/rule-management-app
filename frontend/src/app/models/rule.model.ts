export interface Rule {
  id: number;
  name: string;
  conditions: { [key: string]: any };
  actions: { [key: string]: any };
  description?: string;
  active: boolean;
}

export interface GitRepository {
  url: string;
  branch: string;
  filePath: string;
  username?: string;
  token?: string;
}

export interface PullRequestRequest {
  gitRepo: GitRepository;
  branchName?: string;
  title?: string;
  description?: string;
  commitMessage?: string;
}

export interface ValidationResult {
  valid: boolean;
  errors: string[];
  warnings: string[];
}

export interface SaveToGitResponse {
  message: string;
  branch: string;
  warnings?: string[];
}

export interface CreatePRResponse {
  message: string;
  prUrl: string;
}
