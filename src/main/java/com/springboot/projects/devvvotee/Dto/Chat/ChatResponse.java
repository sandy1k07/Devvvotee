package com.springboot.projects.devvvotee.Dto.Chat;

import com.springboot.projects.devvvotee.Entity.ChatEvent;
import com.springboot.projects.devvvotee.Entity.ChatSession;
import com.springboot.projects.devvvotee.enums.MessageRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;


public record ChatResponse(
        Long id,
        ChatSession chatSession,
        List<ChatEventResponse> chatEvents,
        String content,
        MessageRole role,
        Integer tokensUsed,
        Instant createdAt
){

}
