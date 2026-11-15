package com.org.workspace.dto.response;

public record TaskResponseDto(
        Long id,
        String title,
        String status
) {}