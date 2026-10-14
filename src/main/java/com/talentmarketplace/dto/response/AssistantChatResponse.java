package com.talentmarketplace.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AssistantChatResponse {

    private String answer;
    private Long demandId;
    private Long employeeId;
    private List<ToolInvocationSummary> toolsInvoked;
    private boolean liveModelUsed;
    private String modelName;
    private boolean grounded;
    private LocalDateTime timestamp;

    public AssistantChatResponse() {
        this.toolsInvoked = new ArrayList<>();
        this.timestamp = LocalDateTime.now();
    }

    public AssistantChatResponse(String answer, Long demandId, Long employeeId,
                                 List<ToolInvocationSummary> toolsInvoked,
                                 boolean liveModelUsed, String modelName, boolean grounded) {
        this.answer = answer;
        this.demandId = demandId;
        this.employeeId = employeeId;
        this.toolsInvoked = toolsInvoked != null ? toolsInvoked : new ArrayList<>();
        this.liveModelUsed = liveModelUsed;
        this.modelName = modelName;
        this.grounded = grounded;
        this.timestamp = LocalDateTime.now();
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Long getDemandId() {
        return demandId;
    }

    public void setDemandId(Long demandId) {
        this.demandId = demandId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public List<ToolInvocationSummary> getToolsInvoked() {
        return toolsInvoked;
    }

    public void setToolsInvoked(List<ToolInvocationSummary> toolsInvoked) {
        this.toolsInvoked = toolsInvoked;
    }

    public boolean isLiveModelUsed() {
        return liveModelUsed;
    }

    public void setLiveModelUsed(boolean liveModelUsed) {
        this.liveModelUsed = liveModelUsed;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
