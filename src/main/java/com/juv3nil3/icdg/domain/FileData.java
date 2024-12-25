package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class FileData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String repoName;
    private String filePath; // Full path including the file name

    @OneToMany(mappedBy = "fileData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassData> classes = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "package_id")
    private PackageData packageData;

    // Helper methods for managing the bidirectional relationship
    public void addClass(ClassData classData) {
        classes.add(classData);
        classData.setFileData(this);
    }

    public void removeClass(ClassData classData) {
        classes.remove(classData);
        classData.setFileData(null);
    }

    // Getters and Setters


    public Long getId() {
        return id;
    }

    public PackageData getPackageData() {
        return packageData;
    }

    public void setPackageData(PackageData packageData) {
        this.packageData = packageData;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<ClassData> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassData> classes) {
        this.classes = classes;
    }

    @Override
    public String toString() {
        return "FileData{" +
            "id=" + id +
            ", fileName='" + fileName + '\'' +
            ", repoName='" + repoName + '\'' +
            ", filePath='" + filePath + '\'' +
            ", numberOfClasses=" + (classes != null ? classes.size() : 0) +
            '}';
    }
}
