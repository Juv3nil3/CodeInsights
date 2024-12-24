package com.juv3nil3.icdg.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class AbstractBaseCodeData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdDate;
//
//    @Column(nullable = false)
//    private LocalDateTime updatedDate;

//    @Version
//    @Column(nullable = false)
//    private Integer version;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
