package com.talentmarketplace.ai.tool;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.talentmarketplace.ai.ollama.OllamaTool;
import com.talentmarketplace.ai.tool.AiToolRegistry.ToolExecutionResult;
import com.talentmarketplace.exception.BusinessRuleValidationException;

class AiToolRegistryTest {

    private AiTool tool1;
    private AiTool tool2;
    private AiToolRegistry registry;

    @BeforeEach
    void setUp() {
        tool1 = mock(AiTool.class);
        when(tool1.getName()).thenReturn("get_demand_candidates");
        when(tool1.getOllamaTool()).thenReturn(OllamaTool.function("get_demand_candidates", "test", Map.of()));

        tool2 = mock(AiTool.class);
        when(tool2.getName()).thenReturn("check_employee_availability");
        when(tool2.getOllamaTool()).thenReturn(OllamaTool.function("check_employee_availability", "test", Map.of()));

        registry = new AiToolRegistry(List.of(tool1, tool2));
    }

    @Test
    @DisplayName("isAllowed: returns true for allowlisted tools")
    void isAllowed_AllowedTools() {
        assertThat(registry.isAllowed("get_demand_candidates")).isTrue();
        assertThat(registry.isAllowed("check_employee_availability")).isTrue();
    }

    @Test
    @DisplayName("isAllowed: returns false for unapproved tools")
    void isAllowed_UnapprovedTools() {
        assertThat(registry.isAllowed("execute_sql")).isFalse();
        assertThat(registry.isAllowed("run_shell_command")).isFalse();
        assertThat(registry.isAllowed("confirm_allocation")).isFalse();
        assertThat(registry.isAllowed(null)).isFalse();
    }

    @Test
    @DisplayName("executeTool: throws SecurityException for unauthorized tools")
    void executeTool_UnauthorizedTool_ThrowsSecurityException() {
        assertThatThrownBy(() -> registry.executeTool("execute_arbitrary_code", Map.of()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Unauthorized tool execution: 'execute_arbitrary_code'");
    }

    @Test
    @DisplayName("executeTool: throws BusinessRuleValidationException when tool name is blank")
    void executeTool_BlankToolName_ThrowsException() {
        assertThatThrownBy(() -> registry.executeTool("   ", Map.of()))
                .isInstanceOf(BusinessRuleValidationException.class)
                .hasMessageContaining("Tool name must not be blank");
    }

    @Test
    @DisplayName("executeTool: executes allowed tool and captures execution duration")
    void executeTool_Success() {
        when(tool1.execute(Map.of("demandId", 50))).thenReturn(Map.of("demandId", 50, "status", "OPEN"));

        ToolExecutionResult result = registry.executeTool("get_demand_candidates", Map.of("demandId", 50));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSummary().getToolName()).isEqualTo("get_demand_candidates");
        assertThat(result.getSummary().getExecutionDurationMs()).isGreaterThanOrEqualTo(0);
        assertThat(result.getData()).isInstanceOf(Map.class);
    }

    @Test
    @DisplayName("executeTool: returns error result when tool throws exception without crashing registry")
    void executeTool_ToolFailure() {
        when(tool1.execute(Map.of("demandId", -1))).thenThrow(new IllegalArgumentException("Invalid demand ID"));

        ToolExecutionResult result = registry.executeTool("get_demand_candidates", Map.of("demandId", -1));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getSummary().getErrorMessage()).isEqualTo("Invalid demand ID");
        assertThat(result.getData()).isEqualTo(Map.of("error", "Invalid demand ID"));
    }
}
