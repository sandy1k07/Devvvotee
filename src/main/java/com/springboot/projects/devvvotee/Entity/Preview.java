package com.springboot.projects.devvvotee.Entity;

import com.springboot.projects.devvvotee.enums.PreviewStatus;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

//@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Preview {
    Long id;

    Project project;

    String namespace;
    String podName;
    String previewUrl;

    PreviewStatus status;

    Instant createdAt;
    Instant startedAt;
    Instant endedAt;
}
