package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.ClassData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassDataRepository extends JpaRepository<ClassData, Long> {

    @Query("SELECT c FROM ClassData c LEFT JOIN FETCH c.methods m WHERE c IN :classes")
    List<ClassData> findClassesWithMethods(@Param("classes") List<ClassData> classes);

    @Query("SELECT c FROM ClassData c LEFT JOIN FETCH c.fields f WHERE c IN :classes")
    List<ClassData> findClassesWithFields(@Param("classes") List<ClassData> classes);

}
