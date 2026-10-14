package com.talentmarketplace.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentmarketplace.ai.config.OllamaProperties;
import com.talentmarketplace.ai.ollama.OllamaChatMessage;
import com.talentmarketplace.ai.ollama.OllamaChatRequest;
import com.talentmarketplace.ai.ollama.OllamaChatResponse;
import com.talentmarketplace.ai.ollama.OllamaClient;
import com.talentmarketplace.ai.ollama.OllamaFunctionCall;
import com.talentmarketplace.ai.ollama.OllamaToolCall;
import com.talentmarketplace.ai.tool.AiToolRegistry;
import com.talentmarketplace.dto.request.AssistantChatRequest;
import com.talentmarketplace.dto.response.AssistantChatResponse;
import com.talentmarketplace.dto.response.ToolInvocationSummary;
import com.talentmarketplace.exception.AiAssistantException;

@ExtendWith(MockitoExtension.class)
class AssistantServiceTest {

    @Mock
    private OllamaClient ollamaClient;

    @Mock
    private AiToolRegistry toolRegistry;

    private ObjectMapper objectMapper;
    private OllamaProperties properties;
    private AssistantServiceImpl assistantService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        properties = new OllamaProperties();
        properties.setModel("llama3.2:3b");
        properties.setMaxToolIterations(3);

        lenient().when(ollamaClient.getProperties()).thenReturn(properties);

        assistantService = new AssistantServiceImpl(ollamaClient, toolRegistry, objectMapper);
    }

    @Test
    @DisplayName("chat: rejects unauthorized state-changing prompts without calling model")
    void chat_UnauthorizedStateChange_Rejected() {
        AssistantChatRequest request = new AssistantChatRequest("Please confirm allocation 10 for Marcus Brody");

        AssistantChatResponse response = assistantService.chat(request);

        assertThat(response).isNotNull();
        assertThat(response.isLiveModelUsed()).isFalse();
        assertThat(response.getAnswer()).contains("Unauthorized operation: The AI Assistant operates in read-only analysis mode");
        assertThat(response.getAnswer()).contains("POST /api/allocations/{id}/confirm");
        verify(ollamaClient, never()).chat(any());
    }

    @Test
    @DisplayName("chat: throws AiAssistantException when Ollama is unavailable")
    void chat_OllamaUnavailable_ThrowsException() {
        when(ollamaClient.isAvailable()).thenReturn(false);

        AssistantChatRequest request = new AssistantChatRequest("Find eligible candidates for demand 50");

        assertThatThrownBy(() -> assistantService.chat(request))
                .isInstanceOf(AiAssistantException.class)
                .hasMessageContaining("is currently unreachable or model 'llama3.2:3b' is not installed");
    }

    @Test
    @DisplayName("chat: successful tool invocation loop and grounded explanation")
    void chat_SuccessfulToolInvocationLoop() {
        when(ollamaClient.isAvailable()).thenReturn(true);

        // Turn 1: Model requests tool call
        OllamaToolCall toolCall = new OllamaToolCall("call_1", new OllamaFunctionCall("get_demand_candidates", Map.of("demandId", 50)));
        OllamaChatMessage assistantMsg1 = OllamaChatMessage.assistantWithToolCalls("", List.of(toolCall));
        OllamaChatResponse chatResp1 = new OllamaChatResponse("llama3.2:3b", assistantMsg1, false);

        // Tool execution result
        Map<String, Object> toolData = Map.of(
                "demandId", 50,
                "role", "Senior Java Engineer",
                "topCandidate", Map.of("name", "Aria Stark", "eligible", true, "totalScore", 96.1)
        );
        ToolInvocationSummary summary = ToolInvocationSummary.successful("get_demand_candidates", Map.of("demandId", 50), 35);
        when(toolRegistry.executeTool("get_demand_candidates", Map.of("demandId", 50)))
                .thenReturn(new AiToolRegistry.ToolExecutionResult(toolData, summary, true));

        // Turn 2: Model synthesizes final answer
        OllamaChatMessage assistantMsg2 = OllamaChatMessage.assistant(
                "Based on the recommendation engine, the top candidate is Aria Stark (score 96.1/100, eligible)."
        );
        OllamaChatResponse chatResp2 = new OllamaChatResponse("llama3.2:3b", assistantMsg2, true);

        when(ollamaClient.chat(any(OllamaChatRequest.class)))
                .thenReturn(chatResp1)
                .thenReturn(chatResp2);

        AssistantChatRequest request = new AssistantChatRequest("Find eligible candidates for demand 50");
        AssistantChatResponse response = assistantService.chat(request);

        assertThat(response).isNotNull();
        assertThat(response.isLiveModelUsed()).isTrue();
        assertThat(response.isGrounded()).isTrue();
        assertThat(response.getDemandId()).isEqualTo(50L);
        assertThat(response.getToolsInvoked()).hasSize(1);
        assertThat(response.getToolsInvoked().get(0).getToolName()).isEqualTo("get_demand_candidates");
        assertThat(response.getAnswer()).contains("Aria Stark");
    }

    @Test
    @DisplayName("chat: structured JSON fallback tool selection when native tool calls are absent")
    void chat_StructuredJsonFallbackToolSelection() {
        when(ollamaClient.isAvailable()).thenReturn(true);

        // Model returns structured JSON in message content
        String structuredJson = "{\"tool\": \"check_employee_availability\", \"arguments\": {\"employeeId\": 201, \"startDate\": \"2026-11-01\", \"endDate\": \"2026-11-30\"}}";
        OllamaChatMessage assistantMsg1 = OllamaChatMessage.assistant(structuredJson);
        OllamaChatResponse chatResp1 = new OllamaChatResponse("llama3.2:3b", assistantMsg1, false);

        when(toolRegistry.isAllowed("check_employee_availability")).thenReturn(true);

        Map<String, Object> toolData = Map.of("employeeId", 201, "available", true, "remainingCapacityPercent", 100);
        ToolInvocationSummary summary = ToolInvocationSummary.successful("check_employee_availability", Map.of("employeeId", 201), 20);
        when(toolRegistry.executeTool(any(), any()))
                .thenReturn(new AiToolRegistry.ToolExecutionResult(toolData, summary, true));

        OllamaChatMessage assistantMsg2 = OllamaChatMessage.assistant("Employee 201 is available with 100% capacity.");
        OllamaChatResponse chatResp2 = new OllamaChatResponse("llama3.2:3b", assistantMsg2, true);

        when(ollamaClient.chat(any(OllamaChatRequest.class)))
                .thenReturn(chatResp1)
                .thenReturn(chatResp2);

        AssistantChatRequest request = new AssistantChatRequest("Check whether Devon Vance is available during November 2026");
        AssistantChatResponse response = assistantService.chat(request);

        assertThat(response).isNotNull();
        assertThat(response.isGrounded()).isTrue();
        assertThat(response.getToolsInvoked()).hasSize(1);
        assertThat(response.getAnswer()).contains("Employee 201 is available");
    }

    @Test
    @DisplayName("chat: handles malformed output from model gracefully")
    void chat_MalformedModelOutput_ThrowsException() {
        when(ollamaClient.isAvailable()).thenReturn(true);
        when(ollamaClient.chat(any(OllamaChatRequest.class))).thenReturn(null);

        AssistantChatRequest request = new AssistantChatRequest("Compare candidates for demand 50");

        assertThatThrownBy(() -> assistantService.chat(request))
                .isInstanceOf(AiAssistantException.class)
                .hasMessageContaining("Malformed response received from local Ollama model");
    }

    @Test
    @DisplayName("chat: throws IllegalArgumentException when message is blank")
    void chat_BlankMessage_ThrowsException() {
        AssistantChatRequest request = new AssistantChatRequest("   ");

        assertThatThrownBy(() -> assistantService.chat(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chat message must not be blank");
    }
}
