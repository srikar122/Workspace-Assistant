package com.org.workspace.controller;

import com.org.workspace.agent.ExecutiveAgent;
import com.org.workspace.domain.entity.ChatMessageEntity;
import com.org.workspace.domain.enums.MessageType;
import com.org.workspace.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final ExecutiveAgent executiveAgent;
    private final ChatMessageRepository chatRepository;

    private static final String DEFAULT_USER = "default-exec";

    @PostMapping("/calendar")
    public ResponseEntity<Void> handleCalendarPushNotification(
            @RequestHeader(value = "X-Goog-Channel-ID", required = false) String channelId,
            @RequestHeader(value = "X-Goog-Resource-State", required = false) String resourceState) {

        log.info("Received Google Calendar webhook -> Channel: {}, State: {}", channelId, resourceState);

        if ("sync".equalsIgnoreCase(resourceState)) {
            return ResponseEntity.ok().build();
        }

        if ("exists".equalsIgnoreCase(resourceState)) {
            CompletableFuture.runAsync(this::triggerProactiveAudit);
        }

        return ResponseEntity.ok().build();
    }

    private void triggerProactiveAudit() {
        String auditPrompt = """
            [SYSTEM WEBHOOK TRIGGER]: An external update just occurred on the calendar.
            Call `getUpcomingEvents`, verify if there are any overlapping meetings or conflicts,
            and generate a short status alert for the user.
            """;

        String aiAlert = executiveAgent.execute(DEFAULT_USER, auditPrompt, LocalDateTime.now().toString());

        chatRepository.save(ChatMessageEntity.builder()
                .userId(DEFAULT_USER)
                .type(MessageType.AI)
                .content("🔔 Live Calendar Alert:\n" + aiAlert)
                .createdAt(LocalDateTime.now())
                .build());
    }
}