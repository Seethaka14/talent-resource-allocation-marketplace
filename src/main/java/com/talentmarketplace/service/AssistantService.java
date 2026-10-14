package com.talentmarketplace.service;

import com.talentmarketplace.dto.request.AssistantChatRequest;
import com.talentmarketplace.dto.response.AssistantChatResponse;

public interface AssistantService {

    AssistantChatResponse chat(AssistantChatRequest request);

    boolean isOllamaAvailable();

    String getModelName();
}
