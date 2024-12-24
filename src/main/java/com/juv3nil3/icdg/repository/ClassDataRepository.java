package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.ClassData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassDataRepository extends JpaRepository<ClassData, Long> {}
