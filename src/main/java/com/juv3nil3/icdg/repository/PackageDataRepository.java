package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.PackageData;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageDataRepository extends JpaRepository<PackageData, Long> {
    PackageData findByPackageName(String packageName);
    List<PackageData> findByRepoName(String repoName);
    PackageData findByRepoNameAndPackageName(String repoName, String packageName);
}
