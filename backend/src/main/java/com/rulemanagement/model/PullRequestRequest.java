package com.rulemanagement.model;

public class PullRequestRequest {
    private GitRepository gitRepo;
    private String branchName;
    private String title;
    private String description;
    private String commitMessage;

    public PullRequestRequest() {
    }

    public GitRepository getGitRepo() {
        return gitRepo;
    }

    public void setGitRepo(GitRepository gitRepo) {
        this.gitRepo = gitRepo;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }
}
