package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PackageData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String packageName; // Fully qualified package name

    private String repoName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_package_id")
    private PackageData parentPackage; // Reference to the parent package (null for top-level)

    @OneToMany(mappedBy = "parentPackage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PackageData> subPackages = new ArrayList<>(); // Sub-packages of this package

    @OneToMany(mappedBy = "packageData", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FileData> files = new ArrayList<>();

    public PackageData() {}

    public PackageData(String packageName) {
        this.packageName = packageName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public List<FileData> getFiles() {
        return files;
    }

    public void setFiles(List<FileData> files) {
        this.files = files;
    }

    public void addFile(FileData fileData) {
        this.files.add(fileData);
        fileData.setPackageData(this);
    }

    public PackageData getParentPackage() {
        return parentPackage;
    }

    public void setParentPackage(PackageData parentPackage) {
        this.parentPackage = parentPackage;
    }

    public List<PackageData> getSubPackages() {
        return subPackages;
    }

    public void setSubPackages(List<PackageData> subPackages) {
        this.subPackages = subPackages;
    }
}
