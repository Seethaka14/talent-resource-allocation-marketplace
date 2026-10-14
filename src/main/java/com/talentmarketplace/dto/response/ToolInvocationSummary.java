package com.talentmarketplace.dto.response;

import java.util.Map;

public class ToolInvocationSummary {

    private String toolName;
    private Map<String, Object> arguments;
    private long executionDurationMs;
    private boolean success;
    private String errorMessage;
    private boolean usableEvidence;

    public ToolInvocationSummary() {
    }

    public ToolInvocationSummary(String toolName, Map<String, Object> arguments,
                                 long executionDurationMs, boolean success, String errorMessage) {
        this(toolName, arguments, executionDurationMs, success, errorMessage, success);
    }

    public ToolInvocationSummary(String toolName, Map<String, Object> arguments,
                                 long executionDurationMs, boolean success, String errorMessage, boolean usableEvidence) {
        this.toolName = toolName;
        this.arguments = arguments;
        this.executionDurationMs = executionDurationMs;
        this.success = success;
        this.errorMessage = errorMessage;
        this.usableEvidence = usableEvidence;
    }

    public static ToolInvocationSummary successful(String toolName, Map<String, Object> arguments, long durationMs) {
        return new ToolInvocationSummary(toolName, arguments, durationMs, true, null, true);
    }

    public static ToolInvocationSummary successful(String toolName, Map<String, Object> arguments, long durationMs, boolean usableEvidence) {
        return new ToolInvocationSummary(toolName, arguments, durationMs, true, null, usableEvidence);
    }

    public static ToolInvocationSummary failure(String toolName, Map<String, Object> arguments, long durationMs, String error) {
        return new ToolInvocationSummary(toolName, arguments, durationMs, false, error, false);
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }

    public long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isUsableEvidence() {
        return usableEvidence;
    }

    public void setUsableEvidence(boolean usableEvidence) {
        this.usableEvidence = usableEvidence;
    }
}
