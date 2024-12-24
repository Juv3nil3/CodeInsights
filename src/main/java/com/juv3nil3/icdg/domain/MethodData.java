package com.juv3nil3.icdg.domain;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.List;

@Entity
public class MethodData extends AbstractBaseCodeData {

    private String name;
    private String comment;

    @ElementCollection
    private List<String> annotations;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassData classData;

    public MethodData() {}

    public MethodData(String name, List<String> annotations, String comment, ClassData savedClassData) {
        super();
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

    // Getters and Setters
    public ClassData getClassData() {
        return classData;
    }

    public void setClassData(ClassData classData) {
        this.classData = classData;
    }
}
