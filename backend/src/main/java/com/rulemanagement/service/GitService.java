package com.rulemanagement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rulemanagement.model.GitRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class GitService {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/rule-management";

    public File fetchFileFromGit(GitRepository gitRepo) throws GitAPIException, IOException {
        Path tempPath = Paths.get(TEMP_DIR);
        if (Files.exists(tempPath)) {
            deleteDirectory(tempPath.toFile());
        }
        Files.createDirectories(tempPath);

        Git git;
        if (gitRepo.getUsername() != null && gitRepo.getToken() != null) {
            git = Git.cloneRepository()
                    .setURI(gitRepo.getUrl())
                    .setDirectory(tempPath.toFile())
                    .setBranch(gitRepo.getBranch())
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                            gitRepo.getUsername(), gitRepo.getToken()))
                    .call();
        } else {
            git = Git.cloneRepository()
                    .setURI(gitRepo.getUrl())
                    .setDirectory(tempPath.toFile())
                    .setBranch(gitRepo.getBranch())
                    .call();
        }

        git.close();

        File excelFile = new File(tempPath.toFile(), gitRepo.getFilePath());
        if (!excelFile.exists()) {
            throw new IOException("Excel file not found at path: " + gitRepo.getFilePath());
        }

        return excelFile;
    }

    public String createBranchAndPush(GitRepository gitRepo, File excelFile, String branchName, String commitMessage) 
            throws GitAPIException, IOException {
        
        Path tempPath = Paths.get(TEMP_DIR + "_push");
        if (Files.exists(tempPath)) {
            deleteDirectory(tempPath.toFile());
        }
        Files.createDirectories(tempPath);
        
        Git git;
        UsernamePasswordCredentialsProvider credentials = null;
        if (gitRepo.getUsername() != null && gitRepo.getToken() != null) {
            credentials = new UsernamePasswordCredentialsProvider(
                gitRepo.getUsername(), gitRepo.getToken()
            );
            git = Git.cloneRepository()
                .setURI(gitRepo.getUrl())
                .setDirectory(tempPath.toFile())
                .setBranch(gitRepo.getBranch())
                .setCredentialsProvider(credentials)
                .call();
        } else {
            git = Git.cloneRepository()
                .setURI(gitRepo.getUrl())
                .setDirectory(tempPath.toFile())
                .setBranch(gitRepo.getBranch())
                .call();
        }
        
        git.checkout()
            .setCreateBranch(true)
            .setName(branchName)
            .call();
        
        Path targetPath = tempPath.resolve(gitRepo.getFilePath());
        Files.copy(excelFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        git.add()
            .addFilepattern(gitRepo.getFilePath())
            .call();
        
        git.commit()
            .setMessage(commitMessage)
            .call();
        
        git.push()
            .setCredentialsProvider(credentials)
            .setRemote("origin")
            .call();
        
        git.close();
        deleteDirectory(tempPath.toFile());
        
        return branchName;
    }

    public String createPullRequest(GitRepository gitRepo, String branchName, 
            String prTitle, String prDescription) throws IOException {
        
        String[] parts = gitRepo.getUrl().replace(".git", "").split("/");
        String owner = parts[parts.length - 2];
        String repo = parts[parts.length - 1];
        
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/pulls", owner, repo);
        
        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(
            new PRRequest(prTitle, prDescription, branchName, gitRepo.getBranch())
        );
        
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + gitRepo.getToken());
        headers.set("Accept", "application/vnd.github+json");
        headers.set("Content-Type", "application/json");
        
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, entity, String.class
            );
            
            JsonNode jsonResponse = mapper.readTree(response.getBody());
            return jsonResponse.get("html_url").asText();
        } catch (Exception e) {
            throw new IOException("Failed to create pull request: " + e.getMessage(), e);
        }
    }

    private static class PRRequest {
        public String title;
        public String body;
        public String head;
        public String base;

        public PRRequest(String title, String body, String head, String base) {
            this.title = title;
            this.body = body;
            this.head = head;
            this.base = base;
        }
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}
