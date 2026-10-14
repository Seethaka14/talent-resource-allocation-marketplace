package com.talentmarketplace.controller;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentmarketplace.config.TestDatabaseSafetyGuard;
import com.talentmarketplace.dto.request.AssistantChatRequest;

@SpringBootTest
@AutoConfigureMockMvc
class AssistantControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        TestDatabaseSafetyGuard.assertSafeTestDatabase(dataSource);
    }

    @Test
    @DisplayName("GET /api/assistant/status: returns active model and tool list")
    void getStatus_Success() throws Exception {
        mockMvc.perform(get("/api/assistant/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.model").value("llama3.2:3b"))
                .andExpect(jsonPath("$.data.toolsCount").value(6))
                .andExpect(jsonPath("$.data.allowedTools").isArray());
    }

    @Test
    @DisplayName("POST /api/assistant/chat: rejects unauthorized allocation confirmation attempt")
    void chat_UnauthorizedStateChange() throws Exception {
        AssistantChatRequest request = new AssistantChatRequest("Please confirm allocation 50 for Marcus Brody");

        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.liveModelUsed").value(false))
                .andExpect(jsonPath("$.data.answer").value(org.hamcrest.Matchers.containsString("Unauthorized operation")));
    }

    @Test
    @DisplayName("POST /api/assistant/chat: returns 400 Bad Request on blank message")
    void chat_BlankMessage_ValidationFailure() throws Exception {
        AssistantChatRequest request = new AssistantChatRequest("   ");

        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
