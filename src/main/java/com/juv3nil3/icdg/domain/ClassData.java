package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ClassData extends AbstractBaseCodeData {

    private String name;
    private String comment;

    @ElementCollection
    private List<String> annotations = new ArrayList<>();

    @OneToMany(mappedBy = "classData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MethodData> methods = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private FileData fileData;

    @OneToMany(mappedBy = "classData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FieldData> fields = new ArrayList<>() ;

    public ClassData() {}

    public ClassData(String name, List<String> annotations, String comment, FileData savedFileData) {
        super();
    }

    // Getters and Setters


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public List<MethodData> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodData> methods) {
        this.methods = methods;
    }

    public FileData getFileData() {
        return fileData;
    }

    public void setFileData(FileData fileData) {
        this.fileData = fileData;
    }

    public List<FieldData> getFields() {
        return fields;
    }

    public void setFields(List<FieldData> fields) {
        this.fields = fields;
    }
}
