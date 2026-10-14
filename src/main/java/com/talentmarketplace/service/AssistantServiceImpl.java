package com.talentmarketplace.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentmarketplace.ai.ollama.OllamaChatMessage;
import com.talentmarketplace.ai.ollama.OllamaChatRequest;
import com.talentmarketplace.ai.ollama.OllamaChatResponse;
import com.talentmarketplace.ai.ollama.OllamaClient;
import com.talentmarketplace.ai.ollama.OllamaFunctionCall;
import com.talentmarketplace.ai.ollama.OllamaToolCall;
import com.talentmarketplace.ai.tool.AiToolRegistry;
import com.talentmarketplace.ai.tool.AiToolRegistry.ToolExecutionResult;
import com.talentmarketplace.dto.request.AssistantChatRequest;
import com.talentmarketplace.dto.response.AssistantChatResponse;
import com.talentmarketplace.dto.response.ToolInvocationSummary;
import com.talentmarketplace.exception.AiAssistantException;

@Service
public class AssistantServiceImpl implements AssistantService {

    private static final Logger log = LoggerFactory.getLogger(AssistantServiceImpl.class);

    private static final Pattern STATE_CHANGE_PATTERN = Pattern.compile(
            "\\b(confirm|approve|reject|cancel|delete|update|create|allocate)\\b.*\\b(allocation|allocations|employee|demand)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final String SYSTEM_PROMPT = """
            You are the AI Staffing Agent for the Talent and Resource Allocation Marketplace.
            Your job is to help managers match employees to project demands, evaluate candidate eligibility, explain skill gaps, inspect capacity/availability, and compare recommendations.
            
            STRICT GROUNDING RULES:
            1. Never invent or hallucinate employees, skills, proficiency levels, scores, availability dates, or allocation statuses.
            2. Base all factual statements and recommendations STRICTLY on data retrieved through your available tools.
            3. When a candidate is ineligible or excluded, explain the exact reasons (e.g. missing mandatory skills, insufficient capacity, scheduled leave) provided by the tool.
            4. You are strictly a read-only advisor. You cannot approve, confirm, reject, or cancel allocations.
            5. Provide clear, professional, and concise answers summarizing the tool results.
            """;

    private final OllamaClient ollamaClient;
    private final AiToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    public AssistantServiceImpl(OllamaClient ollamaClient, AiToolRegistry toolRegistry, ObjectMapper objectMapper) {
        this.ollamaClient = ollamaClient;
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isOllamaAvailable() {
        return ollamaClient.isAvailable();
    }

    @Override
    public String getModelName() {
        return ollamaClient.getProperties().getModel();
    }

    @Override
    public AssistantChatResponse chat(AssistantChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("Chat message must not be blank");
        }

        String userMessage = request.getMessage().trim();

        // 1. Safety check: Intercept unauthorized state-changing operations
        if (isStateChangeRequest(userMessage)) {
            return buildUnauthorizedStateChangeResponse(userMessage);
        }

        // 2. Connectivity verification: Verify Ollama is reachable
        if (!ollamaClient.isAvailable()) {
            throw new AiAssistantException(
                    "Local Ollama service at " + ollamaClient.getProperties().getBaseUrl() +
                    " is currently unreachable or model '" + getModelName() + "' is not installed. " +
                    "Verify Ollama is running (`ollama serve`). Live model was not used.",
                    true
            );
        }

        List<OllamaChatMessage> conversation = new ArrayList<>();
        conversation.add(OllamaChatMessage.system(SYSTEM_PROMPT));
        conversation.add(OllamaChatMessage.user(userMessage));

        List<ToolInvocationSummary> toolsInvoked = new ArrayList<>();
        Long referencedDemandId = extractDemandId(userMessage);
        Long referencedEmployeeId = extractEmployeeId(userMessage);

        int maxIterations = ollamaClient.getProperties().getMaxToolIterations();
        int iteration = 0;
        String finalAnswer = null;

        while (iteration < maxIterations) {
            iteration++;
            log.debug("Starting Ollama assistant loop iteration {}/{}", iteration, maxIterations);

            OllamaChatRequest chatRequest = new OllamaChatRequest(
                    getModelName(),
                    conversation,
                    toolRegistry.getOllamaTools()
            );

            OllamaChatResponse chatResponse = ollamaClient.chat(chatRequest);
            if (chatResponse == null || chatResponse.getMessage() == null) {
                throw new AiAssistantException("Malformed response received from local Ollama model.", false);
            }

            OllamaChatMessage assistantMessage = chatResponse.getMessage();
            List<OllamaToolCall> toolCalls = assistantMessage.getToolCalls();

            // If no native tool calls, check for fallback structured JSON in message content
            if ((toolCalls == null || toolCalls.isEmpty()) && assistantMessage.getContent() != null) {
                toolCalls = detectStructuredJsonToolCall(assistantMessage.getContent());
            }

            // If no tool calls requested, the model provided its final response
            if (toolCalls == null || toolCalls.isEmpty()) {
                finalAnswer = assistantMessage.getContent();
                break;
            }

            // Append assistant message with tool calls to conversation
            conversation.add(assistantMessage);

            // Execute each tool call through the allowlist registry
            for (OllamaToolCall toolCall : toolCalls) {
                OllamaFunctionCall func = toolCall.getFunction();
                if (func == null || func.getName() == null) {
                    continue;
                }

                String toolName = func.getName().trim();
                Map<String, Object> arguments = func.getArguments() != null ? func.getArguments() : Collections.emptyMap();

                // Track referenced IDs
                if (arguments.containsKey("demandId") && arguments.get("demandId") != null) {
                    try {
                        referencedDemandId = Long.valueOf(arguments.get("demandId").toString());
                    } catch (NumberFormatException ignored) {}
                }
                if (arguments.containsKey("employeeId") && arguments.get("employeeId") != null) {
                    try {
                        referencedEmployeeId = Long.valueOf(arguments.get("employeeId").toString());
                    } catch (NumberFormatException ignored) {}
                }

                ToolExecutionResult execResult = toolRegistry.executeTool(toolName, arguments);
                toolsInvoked.add(execResult.getSummary());

                String toolResultJson;
                try {
                    toolResultJson = objectMapper.writeValueAsString(execResult.getData());
                } catch (Exception e) {
                    toolResultJson = "{\"error\": \"Failed to serialize tool result: " + e.getMessage() + "\"}";
                }

                conversation.add(OllamaChatMessage.tool(toolResultJson));
            }
        }

        if (finalAnswer == null || finalAnswer.isBlank()) {
            finalAnswer = "Analysis completed using tools: " +
                    toolsInvoked.stream().map(ToolInvocationSummary::getToolName).toList() +
                    ". Please see the retrieved data.";
        }

        return new AssistantChatResponse(
                finalAnswer,
                referencedDemandId,
                referencedEmployeeId,
                toolsInvoked,
                true,
                getModelName(),
                !toolsInvoked.isEmpty()
        );
    }

    private boolean isStateChangeRequest(String message) {
        Matcher matcher = STATE_CHANGE_PATTERN.matcher(message);
        return matcher.find();
    }

    private AssistantChatResponse buildUnauthorizedStateChangeResponse(String userMessage) {
        String answer = "Unauthorized operation: The AI Assistant operates in read-only analysis mode and cannot create, " +
                "confirm, reject, or cancel allocations. All state changes must be performed through the authorized " +
                "Resource Allocation workflow (e.g. POST /api/allocations/{id}/confirm) with explicit user authorization.";
        return new AssistantChatResponse(
                answer,
                extractDemandId(userMessage),
                extractEmployeeId(userMessage),
                Collections.emptyList(),
                false,
                getModelName(),
                false
        );
    }

    private List<OllamaToolCall> detectStructuredJsonToolCall(String content) {
        if (content == null || !content.contains("{") || !content.contains("}")) {
            return Collections.emptyList();
        }

        try {
            int startIdx = content.indexOf('{');
            int endIdx = content.lastIndexOf('}');
            if (startIdx >= 0 && endIdx > startIdx) {
                String candidateJson = content.substring(startIdx, endIdx + 1);
                JsonNode node = objectMapper.readTree(candidateJson);

                String toolName = null;
                if (node.has("name")) {
                    toolName = node.get("name").asText();
                } else if (node.has("tool")) {
                    toolName = node.get("tool").asText();
                }

                if (toolName != null && toolRegistry.isAllowed(toolName)) {
                    Map<String, Object> args = new HashMap<>();
                    if (node.has("arguments") && node.get("arguments").isObject()) {
                        args = objectMapper.convertValue(node.get("arguments"), new TypeReference<Map<String, Object>>() {});
                    } else if (node.has("parameters") && node.get("parameters").isObject()) {
                        args = objectMapper.convertValue(node.get("parameters"), new TypeReference<Map<String, Object>>() {});
                    }
                    OllamaFunctionCall func = new OllamaFunctionCall(toolName, args);
                    OllamaToolCall call = new OllamaToolCall("call_" + System.currentTimeMillis(), func);
                    return List.of(call);
                }
            }
        } catch (Exception ignored) {
        }
        return Collections.emptyList();
    }

    private Long extractDemandId(String text) {
        if (text == null) return null;
        Pattern pattern = Pattern.compile("(?i)\\bdemand(?:\\s+id)?\\s*[:#]?\\s*(\\d+)\\b");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Long.valueOf(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private Long extractEmployeeId(String text) {
        if (text == null) return null;
        Pattern pattern = Pattern.compile("(?i)\\bemployee(?:\\s+id)?\\s*[:#]?\\s*(\\d+)\\b");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                return Long.valueOf(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
