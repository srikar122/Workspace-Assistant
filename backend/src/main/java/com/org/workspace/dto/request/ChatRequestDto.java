package com.org.workspace.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChatRequestDto(
        String userId,
        @NotBlank(message = "Prompt message cannot be empty")
        String message
) {}