package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class MethodData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String comment;

    @ElementCollection
    @CollectionTable(name = "method_data_annotations", joinColumns = @JoinColumn(name = "method_data_id"))
    @Column(name = "annotation")
    private List<String> annotations = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassData classData;

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

    public ClassData getClassData() {
        return classData;
    }

    public void setClassData(ClassData classData) {
        this.classData = classData;
    }
}
