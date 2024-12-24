package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.RepositoryMetadata;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositoryMetadataRepo extends JpaRepository<RepositoryMetadata, Long> {
    Optional<RepositoryMetadata> findByOwnerAndRepoName(String owner, String repoName);
}
