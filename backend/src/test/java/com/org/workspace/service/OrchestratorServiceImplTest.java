package com.org.workspace.service;

import com.org.workspace.agent.ExecutiveAgent;
import com.org.workspace.domain.entity.ChatMessageEntity;
import com.org.workspace.domain.enums.MessageType;
import com.org.workspace.dto.request.ChatRequestDto;
import com.org.workspace.dto.response.ChatResponseDto;
import com.org.workspace.repository.ChatMessageRepository;
import com.org.workspace.service.impl.OrchestratorServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrchestratorServiceImplTest {

    @Mock
    private ExecutiveAgent executiveAgent;

    @Mock
    private ChatMessageRepository chatRepository;

    @InjectMocks
    private OrchestratorServiceImpl orchestratorService;

    @Test
    void shouldPersistUserAndAiMessagesDuringPromptExecution() {
        ChatRequestDto request = new ChatRequestDto("exec-1", "Check my schedule");

        when(executiveAgent.execute(eq("exec-1"), eq("Check my schedule"), anyString()))
                .thenReturn("You have an Architecture Review in 2 hours.");

        ChatMessageEntity savedAiEntity = ChatMessageEntity.builder()
                .id(10L)
                .userId("exec-1")
                .type(MessageType.AI)
                .content("You have an Architecture Review in 2 hours.")
                .createdAt(LocalDateTime.now())
                .build();

        when(chatRepository.save(any(ChatMessageEntity.class))).thenReturn(savedAiEntity);

        ChatResponseDto response = orchestratorService.processUserPrompt(request);

        assertNotNull(response);
        assertEquals("AI", response.type());
        assertEquals("You have an Architecture Review in 2 hours.", response.content());
        verify(chatRepository, times(2)).save(any(ChatMessageEntity.class));
        verify(executiveAgent, times(1)).execute(eq("exec-1"), eq("Check my schedule"), anyString());
    }
}