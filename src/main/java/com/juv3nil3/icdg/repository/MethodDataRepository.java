package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.MethodData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MethodDataRepository extends JpaRepository<MethodData, Long> {}
