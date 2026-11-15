package com.org.workspace.service.impl;

import com.org.workspace.agent.ExecutiveAgent;
import com.org.workspace.domain.entity.ChatMessageEntity;
import com.org.workspace.domain.enums.MessageType;
import com.org.workspace.dto.request.ChatRequestDto;
import com.org.workspace.dto.response.ChatResponseDto;
import com.org.workspace.repository.ChatMessageRepository;
import com.org.workspace.service.OrchestratorService;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrchestratorServiceImpl implements OrchestratorService {

    private final ExecutiveAgent executiveAgent;
    private final ChatMessageRepository chatRepository;
    private final ChatMemoryStore chatMemoryStore;

    @Override
    @Transactional
    public ChatResponseDto processUserPrompt(ChatRequestDto request) {
        String userId = (request.userId() == null || request.userId().isBlank())
                ? "default-exec"
                : request.userId();

        log.info("Processing prompt for user [{}]: {}", userId, request.message());

        String aiReply;
        try {
            // Execute agent first (hydrates prior completed turns from DB and chains tools in memory)
            aiReply = executiveAgent.execute(userId, request.message(), LocalDateTime.now().toString());
        } finally {
            // Always flush intermediate tool-call objects so the next HTTP request starts with clean USER/AI pairs
            chatMemoryStore.deleteMessages(userId);
        }

        chatRepository.save(ChatMessageEntity.builder()
                .userId(userId)
                .type(MessageType.USER)
                .content(request.message())
                .createdAt(LocalDateTime.now())
                .build());

        ChatMessageEntity savedAi = chatRepository.save(ChatMessageEntity.builder()
                .userId(userId)
                .type(MessageType.AI)
                .content(aiReply)
                .createdAt(LocalDateTime.now())
                .build());

        return new ChatResponseDto(
                savedAi.getId(),
                savedAi.getType().name(),
                savedAi.getContent(),
                savedAi.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatResponseDto> getConversationHistory(String userId) {
        return chatRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(m -> new ChatResponseDto(m.getId(), m.getType().name(), m.getContent(), m.getCreatedAt()))
                .toList();
    }
}