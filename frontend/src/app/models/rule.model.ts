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
