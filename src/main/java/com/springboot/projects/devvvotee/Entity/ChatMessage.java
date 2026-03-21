package com.springboot.projects.devvvotee.Entity;

import com.springboot.projects.devvvotee.enums.MessageRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false),
            @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    })
    ChatSession chatSession;

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL)
    @OrderBy("sequence ASC")
    List<ChatEvent> chatEvents; // only for LLM

    @Column(columnDefinition = "text")
    String content;  // only for user

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    MessageRole role;

    Integer tokensUsed;

    @CreationTimestamp
    Instant createdAt;
}
