package com.juv3nil3.icdg.service;

import com.juv3nil3.icdg.domain.*;
import com.juv3nil3.icdg.repository.DocumentationRepository;
import com.juv3nil3.icdg.repository.PackageDataRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.print.Doc;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class DocumentationGenerator {

    private final DocumentationRepository documentationRepository;
    private final PackageDataRepository packageDataRepository;
    private final RepositoryMetadataService repositoryMetadataService;
    private static final Logger logger = LoggerFactory.getLogger(DocumentationGenerator.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public DocumentationGenerator(
        DocumentationRepository documentationRepository,
        PackageDataRepository packageDataRepository,
        RepositoryMetadataService repositoryMetadataService
    ) {
        this.documentationRepository = documentationRepository;
        this.packageDataRepository = packageDataRepository;
        this.repositoryMetadataService = repositoryMetadataService;
    }

    /**
     * Check if documentation already exists for a specific owner and repository.
     *
     * @param owner   the owner of the repository.
     * @param repoName the name of the repository.
     * @return documentation exists
     */
    public Optional<Documentation> findDocumentation(String owner, String repoName) {
        // Retrieve the RepositoryMetadata based on owner and repoName
        Optional<RepositoryMetadata> repositoryMetadataOpt = repositoryMetadataService.findByOwnerAndRepoName(owner, repoName);

        if (repositoryMetadataOpt.isEmpty()) {
            return Optional.empty(); // Return empty if RepositoryMetadata is not found
        }

        // Retrieve the associated documentation using the repository metadata
        return documentationRepository.findByRepositoryMetadata(repositoryMetadataOpt.get());
    }

    public Documentation generateAndSaveDocumentationForRepo(String repoName, String owner, RepositoryMetadata metadata) throws Exception {
        // Step 1: Retrieve all package data for the repository
        List<PackageData> packages = packageDataRepository.findByRepoName(repoName);

        if (packages.isEmpty()) {
            throw new IllegalArgumentException("No data found for repository: " + repoName);
        }
        // Step 2: Merge detached PackageData entities
        List<PackageData> managedPackages = new ArrayList<>();
        for (PackageData packageData : packages) {
            managedPackages.add(entityManager.merge(packageData));
        }

        // Step 3: Create a Documentation object
        Documentation documentation = new Documentation();
        documentation.setRepositoryMetadata(metadata); // Link the metadata
        documentation.setExportPath("/path/to/export"); // Example, adjust as needed
        documentation.setCreatedAt(LocalDateTime.now());
        documentation.setPackages(managedPackages); // Use the managed packages

        // Step 4: Save the documentation object to the database
        documentationRepository.save(documentation);

        return documentation;
    }

    public String exportDocumentation(Documentation documentation) {
        StringBuilder output = new StringBuilder();

        // Add repository name and description
        output.append("### Repository: ").append(documentation.getRepositoryMetadata().getRepoName()).append("\n\n");
        output.append("### Owner: ").append(documentation.getRepositoryMetadata().getOwner()).append("\n\n");
        output.append(documentation.getRepositoryMetadata().getDescription()).append("\n\n");

        // Traverse and format packages, files, classes, methods, and fields
        for (PackageData packageData : documentation.getPackages()) {
            output.append("#### Package: ").append(packageData.getPackageName()).append("\n\n");
            for (FileData file : packageData.getFiles()) {
                output.append("- **File**: ").append(file.getFileName()).append("\n");
                for (ClassData clazz : file.getClasses()) {
                    output.append("  - **Class**: ").append(clazz.getName()).append("\n");
                    output.append("    - **Annotations**: ").append(clazz.getAnnotations()).append("\n");
                    clazz
                        .getMethods()
                        .forEach(method -> {
                            output
                                .append("    - **Method**: ")
                                .append(method.getName())
                                .append(" (")
                                .append(method.getAnnotations())
                                .append(")")
                                .append("\n");
                        });
                    clazz
                        .getFields()
                        .forEach(field -> {
                            output
                                .append("    - **Field**: ")
                                .append(field.getName())
                                .append(" (")
                                .append(field.getAnnotations())
                                .append(")")
                                .append("\n");
                        });
                }
            }
            output.append("\n");
        }

        return output.toString();
    }
}
