package com.juv3nil3.icdg.repository;

import com.juv3nil3.icdg.domain.Documentation;
import com.juv3nil3.icdg.domain.RepositoryMetadata;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentationRepository extends JpaRepository<Documentation, String> {



    Optional<Documentation> findByRepositoryMetadata(@Param("repositoryMetadata")RepositoryMetadata repositoryMetadata);

    @Query("SELECT d FROM Documentation d LEFT JOIN FETCH d.packages p WHERE d.repositoryMetadata = :repoMetadata")
    Optional<Documentation> findDocumentationWithPackages(@Param("repoMetadata") RepositoryMetadata metadata);


//    "JOIN FETCH c.methods m " +
//        "JOIN FETCH c.fields f " +
//    @EntityGraph(attributePaths = {"packages"})
//    Optional<Documentation> findDocumentationWithEntities(@Param("repositoryMetadata") RepositoryMetadata repositoryMetadata);


}
