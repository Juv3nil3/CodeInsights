package com.juv3nil3.icdg.service;

import com.juv3nil3.icdg.domain.RepositoryMetadata;
import com.juv3nil3.icdg.repository.RepositoryMetadataRepo;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepositoryMetadataService {

    private final RepositoryMetadataRepo repositoryMetadataRepository;
    private final GithubService githubService;

    @Autowired
    public RepositoryMetadataService(RepositoryMetadataRepo repositoryMetadataRepository, GithubService githubService) {
        this.repositoryMetadataRepository = repositoryMetadataRepository;
        this.githubService = githubService;
    }

    public RepositoryMetadata saveOrUpdateMetadata(
        String owner,
        String repoName,
        String description,
        String latestCommitHash,
        String defaultBranch
    ) {
        Optional<RepositoryMetadata> existingMetadata = repositoryMetadataRepository.findByOwnerAndRepoName(owner, repoName);

        RepositoryMetadata metadata;
        if (existingMetadata.isPresent()) {
            metadata = existingMetadata.get();
            metadata.setDescription(description);
            metadata.setLatestCommitHash(latestCommitHash);
            metadata.setDefaultBranch(defaultBranch);
        } else {
            metadata = new RepositoryMetadata();
            metadata.setOwner(owner);
            metadata.setRepoName(repoName);
            metadata.setDescription(description);
            metadata.setLatestCommitHash(latestCommitHash);
            metadata.setDefaultBranch(defaultBranch);
        }

        return repositoryMetadataRepository.save(metadata);
    }

    public Optional<RepositoryMetadata> getMetadata(String owner, String repoName) {
        return repositoryMetadataRepository.findByOwnerAndRepoName(owner, repoName);
    }

    /**
     * Find or create RepositoryMetadata for a repository.
     *
     * @param owner    GitHub repository owner.
     * @param repoName GitHub repository name.
     * @return The RepositoryMetadata.
     */
    public RepositoryMetadata findOrfetchMetadata(String owner, String repoName, String accessToken) {
        return repositoryMetadataRepository
            .findByOwnerAndRepoName(owner, repoName)
            .orElseGet(() -> {
                try {
                    return githubService.fetchRepositoryMetadata(owner, repoName, accessToken);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    public Optional<RepositoryMetadata> findByOwnerAndRepoName(String owner, String repoName) {
        return repositoryMetadataRepository.findByOwnerAndRepoName(owner, repoName);
    }
}
