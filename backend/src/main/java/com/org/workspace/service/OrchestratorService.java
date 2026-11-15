package com.org.workspace.service;

import com.org.workspace.dto.request.ChatRequestDto;
import com.org.workspace.dto.response.ChatResponseDto;

import java.util.List;

public interface OrchestratorService {
    ChatResponseDto processUserPrompt(ChatRequestDto request);
    List<ChatResponseDto> getConversationHistory(String userId);
}