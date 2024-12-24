package com.juv3nil3.icdg.service;

import com.juv3nil3.icdg.domain.Documentation;
import com.juv3nil3.icdg.domain.GitHubFile;
import com.juv3nil3.icdg.domain.RepositoryMetadata;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentationGenerationService {

    private final JavaParserService javaParserService;
    private final GithubService githubService;
    private final DocumentationGenerator documentationService;
    private final RepositoryMetadataService repositoryMetadataService;

    private static final Logger logger = LoggerFactory.getLogger(DocumentationGenerationService.class);

    @Autowired
    public DocumentationGenerationService(
        JavaParserService javaParserService,
        GithubService githubService,
        DocumentationGenerator documentationService,
        RepositoryMetadataService repositoryMetadataService
    ) {
        this.javaParserService = javaParserService;
        this.githubService = githubService;
        this.documentationService = documentationService;
        this.repositoryMetadataService = repositoryMetadataService;
    }

    /**
     * Generate and save documentation for a GitHub repository.
     *
     * @param owner       GitHub repository owner.
     * @param repo        GitHub repository name.
     * @param accessToken Personal access token for GitHub API.
     * @throws Exception if an error occurs during the documentation generation.
     */
    public String generateDocumentationForRepo(String owner, String repo, String accessToken) throws Exception {
        // Step 1: Fetch repository metadata
        RepositoryMetadata metadata = repositoryMetadataService.findOrfetchMetadata(owner, repo, accessToken);

        // Step 2: Save or update metadata
        RepositoryMetadata savedMetadata = repositoryMetadataService.saveOrUpdateMetadata(
            metadata.getOwner(),
            metadata.getRepoName(),
            metadata.getDescription(),
            metadata.getLatestCommitHash(),
            metadata.getDefaultBranch()
        );

        // Step 3: Check and handle documentation
        Documentation documentation = getOrGenerateDocumentation(owner, repo, savedMetadata, accessToken);

        // Step 4: Export documentation
        return documentationService.exportDocumentation(documentation);
    }

    /**
     * Get or generate documentation for the repository.
     */
    private Documentation getOrGenerateDocumentation(String owner, String repo, RepositoryMetadata metadata, String accessToken)
        throws Exception {
        // Use findDocumentation to fetch the documentation if it exists
        Optional<Documentation> existingDocumentationOpt = documentationService.findDocumentation(owner, repo);

        if (existingDocumentationOpt.isPresent()) {
            Documentation existingDocumentation = existingDocumentationOpt.get();
            if (isDocumentationOutdated(metadata, existingDocumentation)) {
                generateDocumentation(owner, repo, accessToken);
                return documentationService.generateAndSaveDocumentationForRepo(repo, owner, metadata);
            }
            return existingDocumentation; // Return existing documentation if not outdated
        } else {
            generateDocumentation(owner, repo, accessToken);
            return documentationService.generateAndSaveDocumentationForRepo(repo, owner, metadata);
        }
    }

    /**
     * Check if the documentation is outdated based on commit hash.
     */
    private boolean isDocumentationOutdated(RepositoryMetadata metadata, Documentation documentation) {
        return !metadata.getLatestCommitHash().equals(documentation.getRepositoryMetadata().getLatestCommitHash());
    }

    private void generateDocumentation(String owner, String repo, String accessToken) throws Exception {
        // Fetch all Java files from the repository
        List<GitHubFile> javaFiles = fetchJavaFilesFromRepo(owner, repo, accessToken);

        // Generate documentation for the repository
        for (GitHubFile file : javaFiles) {
            processJavaFile(owner, repo, file, accessToken);
        }
    }

    /**
     * Fetch all Java files from the GitHub repository.
     *
     * @param owner       GitHub repository owner.
     * @param repo        GitHub repository name.
     * @param accessToken Personal access token for GitHub API.
     * @return List of GitHubFile objects representing Java files.
     * @throws Exception if an error occurs during the fetch.
     */
    private List<GitHubFile> fetchJavaFilesFromRepo(String owner, String repo, String accessToken) throws Exception {
        List<GitHubFile> files = githubService.fetchRepositoryFiles(owner, repo, accessToken);
        return files
            .stream()
            .filter(file -> "file".equals(file.getType()) && file.getPath().endsWith(".java"))
            .collect(Collectors.toList());
    }

    /**
     * Process a single Java file to generate and save documentation.
     *
     * @param owner       GitHub repository owner.
     * @param repo        GitHub repository name.
     * @param file        The GitHubFile object representing the Java file.
     * @param accessToken Personal access token for GitHub API.
     * @throws Exception if an error occurs during file processing.
     */
    private void processJavaFile(String owner, String repo, GitHubFile file, String accessToken) throws Exception {
        // Fetch file content from GitHub
        String content = githubService.fetchFileContent(owner, repo, file.getPath(), accessToken);

        // Parse the Java file content
        javaParserService.parseAndSaveFileFromContent(repo, file.getPath(), content);
    }
}
