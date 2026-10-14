package com.talentmarketplace.ai.tool;

import java.util.Map;

import com.talentmarketplace.ai.ollama.OllamaTool;

public interface AiTool {

    String getName();

    String getDescription();

    OllamaTool getOllamaTool();

    Object execute(Map<String, Object> arguments);
}
