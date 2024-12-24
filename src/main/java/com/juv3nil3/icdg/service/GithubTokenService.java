package com.juv3nil3.icdg.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GithubTokenService {

    @Autowired
    private RestTemplate restTemplate;

    // Fetch GitHub token from Keycloak
    public String fetchGithubTokenFromKeycloak(String keycloakAccessToken) throws Exception {
        String url = "http://localhost:9080/realms/jhipster/broker/github/token";

        RestTemplate restTemplate = new RestTemplate();
        // Set the authorization header with the Keycloak access token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + keycloakAccessToken);

        // Create the request entity with headers
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Make the request to Keycloak
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // Get the response body
        String responseBody = response.getBody();

        if (responseBody != null) {
            try {
                // Decode the URL-encoded response
                String decodedResponse = URLDecoder.decode(responseBody, StandardCharsets.UTF_8);

                // Now extract the access token from the decoded response
                String[] params = decodedResponse.split("&");
                for (String param : params) {
                    if (param.startsWith("access_token=")) {
                        // Extract the value after the '=' sign
                        return param.split("=")[1];
                    }
                }
            } catch (Exception e) {
                System.err.println("Error decoding response: " + e.getMessage());
            }
        }

        // Return null if no access token is found or an error occurs
        return null;
    }
}
