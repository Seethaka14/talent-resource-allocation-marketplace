package com.talentmarketplace.ai.ollama;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentmarketplace.ai.config.OllamaProperties;
import com.talentmarketplace.exception.AiAssistantException;

@Component
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);

    private final OllamaProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public OllamaClient(OllamaProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.min(10, properties.getTimeoutSeconds())))
                .build();
    }

    public OllamaClient(OllamaProperties properties, ObjectMapper objectMapper, HttpClient httpClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getBaseUrl().replaceAll("/+$", "") + "/api/tags"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Ollama health check returned non-200 status: {}", response.statusCode());
                return false;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode modelsNode = root.get("models");
            if (modelsNode != null && modelsNode.isArray()) {
                String targetModel = properties.getModel();
                for (JsonNode modelNode : modelsNode) {
                    JsonNode nameNode = modelNode.get("name");
                    if (nameNode != null && (nameNode.asText().equalsIgnoreCase(targetModel)
                            || nameNode.asText().startsWith(targetModel + ":")
                            || targetModel.startsWith(nameNode.asText() + ":"))) {
                        return true;
                    }
                }
                log.warn("Ollama is reachable, but model '{}' is not present in installed models.", targetModel);
            }
            return false;
        } catch (Exception e) {
            log.debug("Ollama is not reachable at {}: {}", properties.getBaseUrl(), e.getMessage());
            return false;
        }
    }

    public OllamaChatResponse chat(OllamaChatRequest request) {
        String url = properties.getBaseUrl().replaceAll("/+$", "") + "/api/chat";
        try {
            String requestJson = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new AiAssistantException(
                        "Model '" + request.getModel() + "' was not found on local Ollama instance at " + properties.getBaseUrl() +
                        ". Please install it using: ollama pull " + request.getModel(), false
                );
            }

            if (response.statusCode() != 200) {
                throw new AiAssistantException(
                        "Ollama API returned HTTP " + response.statusCode() + ": " + response.body(), false
                );
            }

            return objectMapper.readValue(response.body(), OllamaChatResponse.class);

        } catch (HttpConnectTimeoutException | ConnectException e) {
            throw new AiAssistantException(
                    "Cannot connect to local Ollama service at " + properties.getBaseUrl() +
                    ". Verify that Ollama is running (`ollama serve`). Error: " + e.getMessage(), e, true
            );
        } catch (HttpTimeoutException e) {
            throw new AiAssistantException(
                    "Ollama request timed out after " + properties.getTimeoutSeconds() +
                    " seconds while generating response with model '" + request.getModel() + "'.", e, true
            );
        } catch (IOException e) {
            if (e.getCause() instanceof ConnectException) {
                throw new AiAssistantException(
                        "Cannot connect to local Ollama service at " + properties.getBaseUrl() +
                        ". Verify that Ollama is running (`ollama serve`). Error: " + e.getMessage(), e, true
                );
            }
            throw new AiAssistantException("Communication error with local Ollama service: " + e.getMessage(), e, false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiAssistantException("Ollama request was interrupted", e, false);
        }
    }

    public OllamaProperties getProperties() {
        return properties;
    }
}
