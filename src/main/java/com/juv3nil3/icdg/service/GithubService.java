package com.juv3nil3.icdg.service;

import com.juv3nil3.icdg.domain.GitHubFile;
import com.juv3nil3.icdg.domain.RepositoryMetadata;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GithubService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String GITHUB_API_URL = "https://api.github.com/repos/{owner}/{repo}/contents/{path}";
    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);

    // Get the list of files in the repository
    public List<GitHubFile> fetchRepositoryFiles(String owner, String repo, String path, String accessToken) throws Exception {
        logger.info("Fetching files for repository: {}/{} at path: {}", owner, repo, path);

        String url = UriComponentsBuilder.fromUriString(GITHUB_API_URL)
            .buildAndExpand(owner, repo, path) // Expanding placeholders
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<GitHubFile>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<GitHubFile>>() {} // Correctly specify the type parameter
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully fetched files for repository: {}/{}", owner, repo);
                return response.getBody();
            } else {
                logger.error("Failed to fetch files from GitHub API. Status code: {}", response.getStatusCode());
                throw new Exception("Failed to fetch files from GitHub API.");
            }
        } catch (Exception e) {
            logger.error("Error fetching files for repository: {}/{} - {}", owner, repo, e.getMessage());
            throw e;
        }
    }

    public String fetchLatestCommitHash(String owner, String repo, String accessToken) throws Exception {
        logger.info("Fetching latest commit hash for repository: {}/{}", owner, repo);

        String url = String.format("https://api.github.com/repos/%s/%s/commits", owner, repo);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JSONArray commits = new JSONArray(response.getBody());

                if (commits.length() > 0) {
                    String commitHash = commits.getJSONObject(0).getString("sha");
                    logger.info("Successfully fetched latest commit hash: {}", commitHash);
                    return commitHash;
                }
            }

            logger.error("Unable to fetch commits for repository: {}/{}", owner, repo);
            throw new IllegalStateException("Unable to fetch commits for repository: " + owner + "/" + repo);
        } catch (Exception e) {
            logger.error("Error fetching latest commit hash for repository: {}/{} - {}", owner, repo, e.getMessage());
            throw e;
        }
    }

    public RepositoryMetadata fetchRepositoryMetadata(String owner, String repo, String accessToken) throws Exception {
        logger.info("Fetching metadata for repository: {}/{}", owner, repo);

        String url = String.format("https://api.github.com/repos/%s/%s", owner, repo);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject json = new JSONObject(response.getBody());

                String description = json.optString("description", "No description available");
                String latestCommitHash = fetchLatestCommitHash(owner, repo, accessToken);
                String defaultBranch = json.optString("default_branch", "main");

                RepositoryMetadata metadata = new RepositoryMetadata(owner, repo, description, latestCommitHash, defaultBranch);
                logger.info("Successfully fetched metadata for repository: {}/{}", owner, repo);
                return metadata;
            }

            logger.error("Unable to fetch repository metadata for: {}/{}", owner, repo);
            throw new IllegalStateException("Unable to fetch repository metadata for: " + owner + "/" + repo);
        } catch (Exception e) {
            logger.error("Error fetching repository metadata for: {}/{} - {}", owner, repo, e.getMessage());
            throw e;
        }
    }

    // Fetch content of a file
    public String fetchFileContent(String owner, String repo, String filePath, String accessToken) throws Exception {
        logger.info("Fetching file content for file: {} in repository: {}/{}", filePath, owner, repo);

        String url = GITHUB_API_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class, owner, repo, filePath);

            if (response.getStatusCode().is2xxSuccessful()) {
                String decodedContent = decodeBase64(response.getBody());
                logger.info("Successfully fetched file content for file: {} in repository: {}/{}", filePath, owner, repo);
                return decodedContent;
            } else {
                logger.error("Failed to fetch file content from GitHub API for file: {} in repository: {}/{}", filePath, owner, repo);
                throw new Exception("Failed to fetch file from GitHub API.");
            }
        } catch (Exception e) {
            logger.error("Error fetching file content for file: {} in repository: {}/{} - {}", filePath, owner, repo, e.getMessage());
            throw e;
        }
    }

    private String decodeBase64(String encodedContent) {
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(encodedContent);
        return new String(decodedBytes);
    }
}
