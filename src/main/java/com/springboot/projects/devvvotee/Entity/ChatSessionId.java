package com.springboot.projects.devvvotee.Entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChatSessionId implements Serializable {
    Long userId;
    Long projectId;
}
