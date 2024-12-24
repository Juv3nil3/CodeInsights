package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class RepositoryMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String owner; // Owner of the repository
    private String repoName; // Name of the repository
    private String description; // Description of the repository
    private String latestCommitHash; // Latest commit hash
    private String defaultBranch; // Default branch (e.g., "main" or "master")

    private LocalDateTime createdAt; // Timestamp when the metadata was created
    private LocalDateTime updatedAt; // Timestamp when the metadata was last updated

    @OneToOne(mappedBy = "repositoryMetadata", cascade = CascadeType.ALL)
    private Documentation documentation; // Associated documentation

    // Constructors, Getters, and Setters

    public RepositoryMetadata() {}

    public RepositoryMetadata(String owner, String repoName, String description, String latestCommitHash, String defaultBranch) {
        this.owner = owner;
        this.repoName = repoName;
        this.description = description;
        this.latestCommitHash = latestCommitHash;
        this.defaultBranch = defaultBranch;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLatestCommitHash() {
        return latestCommitHash;
    }

    public void setLatestCommitHash(String latestCommitHash) {
        this.latestCommitHash = latestCommitHash;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Documentation getDocumentation() {
        return documentation;
    }

    public void setDocumentation(Documentation documentation) {
        this.documentation = documentation;
    }
}
