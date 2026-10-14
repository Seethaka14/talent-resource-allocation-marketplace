package com.talentmarketplace.ai.ollama;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OllamaChatMessage {

    private String role;
    private String content;

    @JsonProperty("tool_calls")
    private List<OllamaToolCall> toolCalls;

    public OllamaChatMessage() {
    }

    public OllamaChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public OllamaChatMessage(String role, String content, List<OllamaToolCall> toolCalls) {
        this.role = role;
        this.content = content;
        this.toolCalls = toolCalls;
    }

    public static OllamaChatMessage system(String content) {
        return new OllamaChatMessage("system", content);
    }

    public static OllamaChatMessage user(String content) {
        return new OllamaChatMessage("user", content);
    }

    public static OllamaChatMessage assistant(String content) {
        return new OllamaChatMessage("assistant", content);
    }

    public static OllamaChatMessage assistantWithToolCalls(String content, List<OllamaToolCall> toolCalls) {
        return new OllamaChatMessage("assistant", content, toolCalls);
    }

    public static OllamaChatMessage tool(String content) {
        return new OllamaChatMessage("tool", content);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<OllamaToolCall> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<OllamaToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }
}
