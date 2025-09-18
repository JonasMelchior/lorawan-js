package org.cibicom.iot.js.ui.sec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cibicom.iot.js.data.auth.LoginReq;
import org.cibicom.iot.js.data.auth.LoginRes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SecurityService {
    private final HttpClient httpClient;
    @Value("${api.url.prefix}")
    private String urlPrefix;

    public SecurityService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public LoginRes authenticate(String username, String password) throws Exception {
        // Create the LoginReq object
        LoginReq loginReq = new LoginReq(username, password);

        // Convert to JSON using Jackson ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(loginReq);

        // Create the HTTP POST request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlPrefix + "auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Send the request and retrieve the response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Check for successful response (status code 200)
        if (response.statusCode() == 200) {
            // Convert the response body to LoginRes object (assumes response is a JWT token)
            return objectMapper.readValue(response.body(), LoginRes.class);
        } else {
            throw new RuntimeException("Authentication failed with status: " + response.statusCode());
        }
    }
}
