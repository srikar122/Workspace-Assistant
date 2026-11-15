package com.org.workspace.dto.response;

import java.time.LocalDateTime;

public record EventResponseDto(
        String id,
        String summary,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String attendees
) {}