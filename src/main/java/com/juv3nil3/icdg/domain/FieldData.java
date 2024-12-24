package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class FieldData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Field name

    @ElementCollection
    private List<String> annotations = new ArrayList<>(); // Annotations on the field

    @Column
    private String comment; // Optional field comment

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private ClassData classData; // Reference to the parent class

    public FieldData() {}

    public FieldData(String name, List<String> annotations, String comment, ClassData classData) {
        this.name = name;
        this.annotations = annotations;
        this.comment = comment;
        this.classData = classData;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ClassData getClassData() {
        return classData;
    }

    public void setClassData(ClassData classData) {
        this.classData = classData;
    }
}
