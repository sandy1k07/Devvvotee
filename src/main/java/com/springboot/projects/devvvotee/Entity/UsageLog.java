package com.springboot.projects.devvvotee.Entity;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

//@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UsageLog {
    Long id;

    Project project;

    User user;

    String action;
    Integer tokensUsed;
    Integer durationMs;

    String metaData; // json of {model_used, prompt used}

    Instant createdAt;
}
