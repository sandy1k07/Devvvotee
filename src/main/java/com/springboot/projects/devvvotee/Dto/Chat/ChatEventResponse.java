package com.springboot.projects.devvvotee.Dto.Chat;


import com.springboot.projects.devvvotee.enums.ChatEventType;
import jakarta.persistence.*;

public record ChatEventResponse(
        Long id,
        Integer sequence,
        String content,
        String filePath,
        String metadata,
        ChatEventType type
) {
}
