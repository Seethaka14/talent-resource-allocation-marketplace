package com.talentmarketplace.ai.ollama;

import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentmarketplace.ai.config.OllamaProperties;
import com.talentmarketplace.exception.AiAssistantException;

@ExtendWith(MockitoExtension.class)
class OllamaClientTest {

    @Mock
    private HttpClient mockHttpClient;

    private OllamaProperties properties;
    private ObjectMapper objectMapper;
    private OllamaClient ollamaClient;

    @BeforeEach
    void setUp() {
        properties = new OllamaProperties();
        properties.setBaseUrl("http://localhost:11434");
        properties.setModel("llama3.2:3b");
        properties.setTimeoutSeconds(5);
        objectMapper = new ObjectMapper();
        ollamaClient = new OllamaClient(properties, objectMapper, mockHttpClient);
    }

    @Test
    @DisplayName("isAvailable: returns true when Ollama returns 200 with matching model")
    void isAvailable_Success() throws Exception {
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"models\": [{\"name\": \"llama3.2:3b\"}, {\"name\": \"llama2:latest\"}]}");

        doReturn(mockResponse).when(mockHttpClient).send(any(HttpRequest.class), any());

        assertThat(ollamaClient.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("isAvailable: returns false when target model is not present in Ollama")
    void isAvailable_MissingModel() throws Exception {
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"models\": [{\"name\": \"mistral:latest\"}]}");

        doReturn(mockResponse).when(mockHttpClient).send(any(HttpRequest.class), any());

        assertThat(ollamaClient.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("isAvailable: returns false when Ollama connection fails")
    void isAvailable_ConnectionFailure() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenThrow(new ConnectException("Connection refused"));

        assertThat(ollamaClient.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("chat: returns parsed OllamaChatResponse on HTTP 200")
    void chat_Success() throws Exception {
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("""
                {
                    "model": "llama3.2:3b",
                    "message": {
                        "role": "assistant",
                        "content": "Aria Stark is the top candidate for demand 50."
                    },
                    "done": true
                }
                """);

        doReturn(mockResponse).when(mockHttpClient).send(any(HttpRequest.class), any());

        OllamaChatRequest request = new OllamaChatRequest("llama3.2:3b", List.of(OllamaChatMessage.user("test")), Collections.emptyList());
        OllamaChatResponse response = ollamaClient.chat(request);

        assertThat(response).isNotNull();
        assertThat(response.getModel()).isEqualTo("llama3.2:3b");
        assertThat(response.getMessage().getContent()).contains("Aria Stark");
    }

    @Test
    @DisplayName("chat: throws AiAssistantException on 404 Model Not Found")
    void chat_ModelNotFound() throws Exception {
        @SuppressWarnings("unchecked")
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(404);

        doReturn(mockResponse).when(mockHttpClient).send(any(HttpRequest.class), any());

        OllamaChatRequest request = new OllamaChatRequest("unknown-model", List.of(OllamaChatMessage.user("test")), Collections.emptyList());

        assertThatThrownBy(() -> ollamaClient.chat(request))
                .isInstanceOf(AiAssistantException.class)
                .hasMessageContaining("was not found on local Ollama instance");
    }

    @Test
    @DisplayName("chat: throws AiAssistantException with connectionFailure=true on timeout")
    void chat_Timeout() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenThrow(new HttpTimeoutException("request timed out"));

        OllamaChatRequest request = new OllamaChatRequest("llama3.2:3b", List.of(OllamaChatMessage.user("test")), Collections.emptyList());

        assertThatThrownBy(() -> ollamaClient.chat(request))
                .isInstanceOf(AiAssistantException.class)
                .hasMessageContaining("timed out");
    }

    @Test
    @DisplayName("chat: throws AiAssistantException with connectionFailure=true on connect exception")
    void chat_ConnectException() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any()))
                .thenThrow(new ConnectException("Connection refused: connect"));

        OllamaChatRequest request = new OllamaChatRequest("llama3.2:3b", List.of(OllamaChatMessage.user("test")), Collections.emptyList());

        assertThatThrownBy(() -> ollamaClient.chat(request))
                .isInstanceOf(AiAssistantException.class)
                .hasMessageContaining("Cannot connect to local Ollama service");
    }
}
