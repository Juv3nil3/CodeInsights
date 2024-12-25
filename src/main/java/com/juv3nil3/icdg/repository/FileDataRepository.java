package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.FileData;
import com.juv3nil3.icdg.domain.PackageData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileDataRepository extends JpaRepository<FileData, Long> {

    @Query("SELECT f FROM FileData f LEFT JOIN FETCH f.classes c WHERE f.packageData IN :packages")
    List<FileData> findFilesWithClasses(@Param("packages") List<PackageData> packages);


}
