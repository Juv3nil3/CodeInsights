package com.juv3nil3.icdg.service;

import com.juv3nil3.icdg.domain.*;
import com.juv3nil3.icdg.repository.ClassDataRepository;
import com.juv3nil3.icdg.repository.DocumentationRepository;
import com.juv3nil3.icdg.repository.FileDataRepository;
import com.juv3nil3.icdg.repository.PackageDataRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.print.Doc;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
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
    private final FileDataRepository fileDataRepository;
    private final ClassDataRepository classRepository;
    private final RepositoryMetadataService repositoryMetadataService;
    private static final Logger logger = LoggerFactory.getLogger(DocumentationGenerator.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public DocumentationGenerator(
        DocumentationRepository documentationRepository,
        PackageDataRepository packageDataRepository, FileDataRepository fileDataRepository, ClassDataRepository classRepository,
        RepositoryMetadataService repositoryMetadataService
    ) {
        this.documentationRepository = documentationRepository;
        this.packageDataRepository = packageDataRepository;
        this.fileDataRepository = fileDataRepository;
        this.classRepository = classRepository;
        this.repositoryMetadataService = repositoryMetadataService;
    }

    /**
     * Check if documentation already exists for a specific owner and repository.
     *
     * @param owner   the owner of the repository.
     * @param repoName the name of the repository.
     * @return documentation exists
     */
    @Transactional
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

    @Transactional
    public String exportDocumentation(Documentation documentation) {
        StringBuilder output = new StringBuilder();

        logger.debug("Entering exportDocumentation() with documentation: {}", documentation);

        // Ensure documentation is eagerly loaded
        Documentation loadedDocumentation = ensureDocumentationLoaded(documentation);

        // Add repository name and description
        output.append("### Repository: ").append(loadedDocumentation.getRepositoryMetadata().getRepoName()).append("\n\n");
        output.append("### Owner: ").append(loadedDocumentation.getRepositoryMetadata().getOwner()).append("\n\n");
        output.append(loadedDocumentation.getRepositoryMetadata().getDescription()).append("\n\n");

        // Traverse and format packages, files, classes, methods, and fields
        for (PackageData packageData : loadedDocumentation.getPackages()) {
            output.append("#### Package: ").append(packageData.getPackageName()).append("\n\n");
            for (FileData file : packageData.getFiles()) {
                output.append("- **File**: ").append(file.getFileName()).append("\n");
                for (ClassData clazz : file.getClasses()) {
                    output.append("  - **Class**: ").append(clazz.getName()).append("\n");
                    output.append("    - **Annotations**: ").append(clazz.getAnnotations()).append("\n");
                    clazz.getMethods().forEach(method -> {
                        output.append("    - **Method**: ").append(method.getName())
                            .append(" (").append(method.getAnnotations()).append(")\n");
                    });
                    clazz.getFields().forEach(field -> {
                        output.append("    - **Field**: ").append(field.getName())
                            .append(" (").append(field.getAnnotations()).append(")\n");
                    });
                }
            }
            output.append("\n");
        }

        logger.debug("Completed exportDocumentation().");
        return output.toString();
    }

    // Method to check if the related collections (files, classes, methods, fields) are initialized
    private boolean isDocumentationFullyInitialized(Documentation documentation) {
        logger.debug("Checking if documentation is fully initialized...");

        if (documentation.getPackages() == null || documentation.getPackages().isEmpty()) {
            logger.debug("Packages collection is not initialized or empty.");
            return false;
        }

        // Check if the packages and their related entities (files, classes, methods, fields) are initialized
        for (PackageData packageData : documentation.getPackages()) {
            if (packageData.getFiles() == null || packageData.getFiles().isEmpty()) {
                logger.debug("Files collection is not initialized or empty for package: {}", packageData.getPackageName());
                return false;
            }
            for (FileData file : packageData.getFiles()) {
                if (file.getClasses() == null || file.getClasses().isEmpty()) {
                    logger.debug("Classes collection is not initialized or empty for file: {}", file.getFileName());
                    return false;
                }
                for (ClassData clazz : file.getClasses()) {
                    if (clazz.getMethods() == null || clazz.getMethods().isEmpty()) {
                        logger.debug("Methods collection is not initialized or empty for class: {}", clazz.getName());
                        return false;
                    }
                    if (clazz.getFields() == null || clazz.getFields().isEmpty()) {
                        logger.debug("Fields collection is not initialized or empty for class: {}", clazz.getName());
                        return false;
                    }
                }
            }
        }

        logger.debug("Documentation and all related entities are fully initialized.");
        return true;
    }


    @Transactional
    public Documentation ensureDocumentationLoaded(Documentation documentation) {
        logger.debug("Ensuring documentation is loaded with all related entities...");

        if (!isDocumentationFullyInitialized(documentation)) {
            logger.debug("Packages not initialized, loading eagerly...");

            Documentation eagerlyLoadedDoc = loadDocumentationWithPackages(documentation);

            // Load and associate files with packages
            associateFilesWithPackages(eagerlyLoadedDoc);

            // Fetch methods and fields for classes
            fetchAndInitializeClassDetails(eagerlyLoadedDoc);

            return eagerlyLoadedDoc;
        } else {
            logger.debug("Packages already initialized, proceeding.");
            return documentation;
        }
    }

    private Documentation loadDocumentationWithPackages(Documentation documentation) {
        Optional<Documentation> docOpt = documentationRepository.findDocumentationWithPackages(documentation.getRepositoryMetadata());
        if (docOpt.isPresent()) {
            logger.debug("Eagerly loaded documentation with packages.");
            Documentation doc = docOpt.get();
            documentation.setPackages(doc.getPackages());
            return documentation;
        } else {
            logger.error("Documentation not found for repository: {}", documentation.getRepositoryMetadata().getRepoName());
            throw new EntityNotFoundException("Documentation not found");
        }
    }

    private void associateFilesWithPackages(Documentation documentation) {
        List<FileData> files = fileDataRepository.findFilesWithClasses(documentation.getPackages());
        Map<String, List<FileData>> filesByPackage = files.stream()
            .collect(Collectors.groupingBy(file -> file.getPackageData().getPackageName()));

        documentation.getPackages().forEach(packageData -> {
            List<FileData> filesForPackage = filesByPackage.getOrDefault(packageData.getPackageName(), Collections.emptyList());
            packageData.getFiles().addAll(filesForPackage);
            logger.info("Package: {} has {} files", packageData.getPackageName(), filesForPackage.size());
        });
    }

    private void fetchAndInitializeClassDetails(Documentation documentation) {
        List<ClassData> classes = documentation.getPackages().stream()
            .flatMap(pkg -> pkg.getFiles().stream())
            .flatMap(file -> file.getClasses().stream())
            .collect(Collectors.toList());

        classRepository.findClassesWithMethods(classes).forEach(clazz -> clazz.getMethods().size());
        classRepository.findClassesWithFields(classes).forEach(clazz -> clazz.getFields().size());
    }


}
