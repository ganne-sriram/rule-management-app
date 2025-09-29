package com.rulemanagement.service;

import com.rulemanagement.model.GitRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
