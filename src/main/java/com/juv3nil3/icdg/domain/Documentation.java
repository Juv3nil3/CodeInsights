package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Documentation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "repository_metadata_id")
    private RepositoryMetadata repositoryMetadata; // Associated repository metadata

    private String exportPath; // Path where documentation is exported
    private LocalDateTime createdAt; // Timestamp when the documentation was created
    private LocalDateTime updatedAt; // Timestamp when the documentation was last updated

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "documentation_packages", // Join table name
        joinColumns = @JoinColumn(name = "documentation_id"), // Foreign key to Documentation
        inverseJoinColumns = @JoinColumn(name = "package_data_id")
    )
    private List<PackageData> packages = new ArrayList<>();

    // Getters and Setters

    public RepositoryMetadata getRepositoryMetadata() {
        return repositoryMetadata;
    }

    public void setRepositoryMetadata(RepositoryMetadata repositoryMetadata) {
        this.repositoryMetadata = repositoryMetadata;
    }

    public String getExportPath() {
        return exportPath;
    }

    public void setExportPath(String exportPath) {
        this.exportPath = exportPath;
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

    public List<PackageData> getPackages() {
        return packages;
    }

    public void setPackages(List<PackageData> packages) {
        this.packages = packages;
    }
}
