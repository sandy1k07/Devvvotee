package com.springboot.projects.devvvotee.Dto.Project;

import com.springboot.projects.devvvotee.enums.ProjectRole;

import java.time.Instant;

public record ProjectSummaryResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}
