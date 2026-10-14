package com.talentmarketplace.ai.ollama;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OllamaChatRequest {

    private String model;
    private List<OllamaChatMessage> messages;
    private List<OllamaTool> tools;
    private boolean stream = false;

    public OllamaChatRequest() {
    }

    public OllamaChatRequest(String model, List<OllamaChatMessage> messages, List<OllamaTool> tools) {
        this.model = model;
        this.messages = messages;
        this.tools = tools;
        this.stream = false;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<OllamaChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<OllamaChatMessage> messages) {
        this.messages = messages;
    }

    public List<OllamaTool> getTools() {
        return tools;
    }

    public void setTools(List<OllamaTool> tools) {
        this.tools = tools;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }
}
