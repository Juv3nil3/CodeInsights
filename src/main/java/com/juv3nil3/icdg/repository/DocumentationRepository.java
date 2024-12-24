package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.Documentation;
import com.juv3nil3.icdg.domain.RepositoryMetadata;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentationRepository extends JpaRepository<Documentation, String> {
    Optional<Documentation> findByRepositoryMetadata(RepositoryMetadata repositoryMetadata);
}
