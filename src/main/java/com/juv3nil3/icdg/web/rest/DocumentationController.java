package com.juv3nil3.icdg.web.rest;

import com.juv3nil3.icdg.service.DocumentationGenerationService;
import com.juv3nil3.icdg.service.GithubService;
import com.juv3nil3.icdg.service.GithubTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/documentation")
public class DocumentationController {

    private static final String KEYCLOAK_HOST = "http://localhost:9080"; // Your Keycloak server URL
    private static final String REALM_NAME = "jhipster"; // Your Keycloak realm name
    private static final String PROVIDER_ALIAS = "github"; // The alias for GitHub IDP in Keycloak

    @Autowired
    private RestTemplate restTemplate; // Used for making HTTP calls to Keycloak and GitHub

    private final DocumentationGenerationService generationService;
    private final GithubTokenService githubTokenService;

    @Autowired
    public DocumentationController(DocumentationGenerationService generationService, GithubTokenService githubTokenService) {
        this.generationService = generationService;
        this.githubTokenService = githubTokenService;
    }

    @GetMapping("/generate")
    public ResponseEntity<?> generateDocumentationForRepo(
        @RequestHeader("Authorization") String keycloakAccessToken,
        @RequestParam String owner,
        @RequestParam String repo
    ) {
        try {
            // Step 1: Extract the access token from the Authorization header
            String accessToken = keycloakAccessToken.replace("Bearer ", "");

            // Step 2: Fetch the GitHub token from Keycloak using the access token
            String githubToken = githubTokenService.fetchGithubTokenFromKeycloak(accessToken);

            // Step 3: Generate documentation for the GitHub repository
            String documentation = generationService.generateDocumentationForRepo(owner, repo, githubToken);

            // Return success response
            return ResponseEntity.status(HttpStatus.OK).body(documentation); // HTTP status code for successful retrieval
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating documentation: " + e.getMessage());
        }
    }
}
