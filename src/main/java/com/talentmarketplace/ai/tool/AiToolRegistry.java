package com.talentmarketplace.ai.tool;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.talentmarketplace.ai.ollama.OllamaTool;
import com.talentmarketplace.dto.response.ToolInvocationSummary;
import com.talentmarketplace.exception.BusinessRuleValidationException;

@Component
public class AiToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(AiToolRegistry.class);

    private final Map<String, AiTool> allowedTools;

    public AiToolRegistry(List<AiTool> tools) {
        Map<String, AiTool> map = new HashMap<>();
        for (AiTool tool : tools) {
            map.put(tool.getName(), tool);
        }
        this.allowedTools = Collections.unmodifiableMap(map);
        log.info("Initialized AiToolRegistry with {} allowed tools: {}", allowedTools.size(), allowedTools.keySet());
    }

    public boolean isAllowed(String toolName) {
        return toolName != null && allowedTools.containsKey(toolName.trim());
    }

    public List<OllamaTool> getOllamaTools() {
        return allowedTools.values().stream()
                .map(AiTool::getOllamaTool)
                .collect(Collectors.toList());
    }

    public ToolExecutionResult executeTool(String toolName, Map<String, Object> arguments) {
        if (toolName == null || toolName.isBlank()) {
            throw new BusinessRuleValidationException("Tool name must not be blank");
        }

        String normalizedName = toolName.trim();
        if (!allowedTools.containsKey(normalizedName)) {
            String msg = "Unauthorized tool execution: '" + normalizedName + "' is not in the allowed tools list. " +
                    "Allowed tools: " + allowedTools.keySet();
            log.warn(msg);
            throw new SecurityException(msg);
        }

        AiTool tool = allowedTools.get(normalizedName);
        long startTime = System.currentTimeMillis();
        try {
            Object result = tool.execute(arguments != null ? arguments : Collections.emptyMap());
            long duration = System.currentTimeMillis() - startTime;
            boolean usable = isUsableEvidence(result);
            ToolInvocationSummary summary = ToolInvocationSummary.successful(normalizedName, arguments, duration, usable);
            return new ToolExecutionResult(result, summary, true);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("Tool '{}' execution failed: {}", normalizedName, e.getMessage());
            ToolInvocationSummary summary = ToolInvocationSummary.failure(normalizedName, arguments, duration, e.getMessage());
            return new ToolExecutionResult(Map.of("error", e.getMessage()), summary, false);
        }
    }

    private boolean isUsableEvidence(Object result) {
        if (result == null) {
            return false;
        }
        if (result instanceof java.util.Collection<?> col) {
            return !col.isEmpty();
        }
        if (result instanceof Map<?, ?> map) {
            if (map.isEmpty() || map.containsKey("error")) {
                return false;
            }
            if (map.containsKey("candidates") && map.get("candidates") instanceof java.util.Collection<?> cList) {
                return !cList.isEmpty();
            }
            return true;
        }
        if (result instanceof String str) {
            return !str.isBlank();
        }
        return true;
    }

    public Map<String, AiTool> getAllowedTools() {
        return allowedTools;
    }

    public static class ToolExecutionResult {
        private final Object data;
        private final ToolInvocationSummary summary;
        private final boolean success;

        public ToolExecutionResult(Object data, ToolInvocationSummary summary, boolean success) {
            this.data = data;
            this.summary = summary;
            this.success = success;
        }

        public Object getData() {
            return data;
        }

        public ToolInvocationSummary getSummary() {
            return summary;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
