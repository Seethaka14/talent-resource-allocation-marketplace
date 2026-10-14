package com.talentmarketplace.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentmarketplace.ai.tool.AiToolRegistry;
import com.talentmarketplace.dto.request.AssistantChatRequest;
import com.talentmarketplace.dto.response.ApiResponse;
import com.talentmarketplace.dto.response.AssistantChatResponse;
import com.talentmarketplace.service.AssistantService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;
    private final AiToolRegistry toolRegistry;

    public AssistantController(AssistantService assistantService, AiToolRegistry toolRegistry) {
        this.assistantService = assistantService;
        this.toolRegistry = toolRegistry;
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AssistantChatResponse>> chat(
            @Valid @RequestBody AssistantChatRequest request) {
        AssistantChatResponse response = assistantService.chat(request);
        return ResponseEntity.ok(ApiResponse.ok("Assistant response generated successfully", response));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus() {
        boolean available = assistantService.isOllamaAvailable();
        Map<String, Object> status = Map.of(
                "ollamaReachable", available,
                "model", assistantService.getModelName(),
                "toolsCount", toolRegistry.getAllowedTools().size(),
                "allowedTools", toolRegistry.getAllowedTools().keySet()
        );
        return ResponseEntity.ok(ApiResponse.ok("AI Assistant status", status));
    }
}
