package com.org.workspace.dto.response;

import java.time.LocalDateTime;

public record ChatResponseDto(
        Long id,
        String type,
        String content,
        LocalDateTime createdAt
) {}