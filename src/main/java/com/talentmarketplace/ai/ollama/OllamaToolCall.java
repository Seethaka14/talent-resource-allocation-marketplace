package com.talentmarketplace.ai.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaToolCall {

    private String id;
    private String type = "function";
    private OllamaFunctionCall function;

    public OllamaToolCall() {
    }

    public OllamaToolCall(String id, OllamaFunctionCall function) {
        this.id = id;
        this.function = function;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public OllamaFunctionCall getFunction() {
        return function;
    }

    public void setFunction(OllamaFunctionCall function) {
        this.function = function;
    }
}
