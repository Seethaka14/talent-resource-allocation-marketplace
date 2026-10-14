package com.talentmarketplace.ai.ollama;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OllamaTool {

    private String type = "function";
    private OllamaFunctionDefinition function;

    public OllamaTool() {
    }

    public OllamaTool(OllamaFunctionDefinition function) {
        this.function = function;
    }

    public static OllamaTool function(String name, String description, java.util.Map<String, Object> parameters) {
        return new OllamaTool(new OllamaFunctionDefinition(name, description, parameters));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public OllamaFunctionDefinition getFunction() {
        return function;
    }

    public void setFunction(OllamaFunctionDefinition function) {
        this.function = function;
    }
}
