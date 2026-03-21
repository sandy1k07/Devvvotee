package com.springboot.projects.devvvotee.Dto.Chat;

public record ChatRequest(
        String message,
        Long projectId
) {
}
