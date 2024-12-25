package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ClassData{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String comment;

    @ElementCollection
    @CollectionTable(name = "class_data_annotations", joinColumns = @JoinColumn(name = "class_data_id"))
    @Column(name = "annotation")
    private List<String> annotations = new ArrayList<>();

    @OneToMany(mappedBy = "classData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MethodData> methods = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private FileData fileData;

    @OneToMany(mappedBy = "classData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FieldData> fields = new ArrayList<>() ;

    public ClassData() {}


    // Getters and Setters


    public Long getId() {
        return id;
    }

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

    @Override
    public String toString() {
        return "ClassData{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", comment='" + comment + '\'' +
            ", annotations=" + annotations +
            ", numberOfFields=" + (fields != null ? fields.size() : 0) +
            ", numberOfMethods=" + (methods != null ? methods.size() : 0) +
            '}';
    }
}
