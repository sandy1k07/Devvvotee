package com.springboot.projects.devvvotee.Entity;

import com.springboot.projects.devvvotee.enums.ChatEventType;
import com.springboot.projects.devvvotee.enums.LlmMessageAttribute;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "chat_events")
public class ChatEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatMessage_id", nullable = false)
    ChatMessage chatMessage;

    @Column(nullable = false)
    Integer sequence;

    @Column(columnDefinition = "text")
    String content; // only for <message>

    @Enumerated(EnumType.STRING)
    LlmMessageAttribute messageAttribute; // only for <message>

    String filePath; // only for <file>

    @Column(columnDefinition = "text")
    String metadata;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    ChatEventType type;
}
