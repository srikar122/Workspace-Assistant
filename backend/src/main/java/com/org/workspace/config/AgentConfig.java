package com.org.workspace.config;

import com.org.workspace.domain.entity.ChatMessageEntity;
import com.org.workspace.domain.enums.MessageType;
import com.org.workspace.repository.ChatMessageRepository;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class AgentConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model-name:gemini-3.5-flash-lite}") String modelName) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.1)
                .build();
    }

    @Bean
    public ChatMemoryStore chatMemoryStore(ChatMessageRepository chatRepository) {
        return new ChatMemoryStore() {
            private final Map<Object, List<ChatMessage>> memoryCache = new ConcurrentHashMap<>();

            @Override
            public List<ChatMessage> getMessages(Object memoryId) {
                return memoryCache.computeIfAbsent(memoryId, id -> {
                    String userId = id.toString();
                    List<ChatMessageEntity> entities = chatRepository.findByUserIdOrderByCreatedAtAsc(userId);
                    List<ChatMessage> cleanHistory = new ArrayList<>();

                    // Build strictly alternating USER -> AI pairs from completed turns
                    MessageType expectedType = MessageType.USER;
                    for (ChatMessageEntity entity : entities) {
                        if (entity.getContent() == null || entity.getContent().isBlank()) {
                            continue;
                        }
                        if (entity.getType() == MessageType.USER) {
                            if (expectedType == MessageType.AI && !cleanHistory.isEmpty()) {
                                // Replace previous unanswered USER message from a failed turn
                                cleanHistory.remove(cleanHistory.size() - 1);
                            }
                            cleanHistory.add(UserMessage.from(entity.getContent()));
                            expectedType = MessageType.AI;
                        } else if (entity.getType() == MessageType.AI && expectedType == MessageType.AI) {
                            cleanHistory.add(AiMessage.from(entity.getContent()));
                            expectedType = MessageType.USER;
                        }
                    }

                    // Ensure hydrated history ends on an AI turn so the next incoming message is a USER turn
                    if (!cleanHistory.isEmpty() && cleanHistory.get(cleanHistory.size() - 1) instanceof UserMessage) {
                        cleanHistory.remove(cleanHistory.size() - 1);
                    }

                    // Keep only the last 10 messages (5 pairs) so the window never slices in-flight tool calls
                    int size = cleanHistory.size();
                    if (size > 10) {
                        cleanHistory = new ArrayList<>(cleanHistory.subList(size - 10, size));
                    }

                    return cleanHistory;
                });
            }

            @Override
            public void updateMessages(Object memoryId, List<ChatMessage> messages) {
                memoryCache.put(memoryId, new ArrayList<>(messages));
            }

            @Override
            public void deleteMessages(Object memoryId) {
                memoryCache.remove(memoryId);
            }
        };
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore chatMemoryStore) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(40)
                .chatMemoryStore(chatMemoryStore)
                .build();
    }
}