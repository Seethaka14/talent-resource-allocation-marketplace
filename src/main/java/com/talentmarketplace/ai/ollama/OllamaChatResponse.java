package com.talentmarketplace.ai.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaChatResponse {

    private String model;
    private OllamaChatMessage message;
    private boolean done;

    @JsonProperty("done_reason")
    private String doneReason;

    public OllamaChatResponse() {
    }

    public OllamaChatResponse(String model, OllamaChatMessage message, boolean done) {
        this.model = model;
        this.message = message;
        this.done = done;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public OllamaChatMessage getMessage() {
        return message;
    }

    public void setMessage(OllamaChatMessage message) {
        this.message = message;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getDoneReason() {
        return doneReason;
    }

    public void setDoneReason(String doneReason) {
        this.doneReason = doneReason;
    }
}
