package com.springboot.projects.devvvotee.Dto.Project;

import com.springboot.projects.devvvotee.Dto.Auth.UserProfileResponse;
import com.springboot.projects.devvvotee.enums.ProjectRole;

import java.time.Instant;

public record ProjectResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        ProjectRole projectRole
) {
}
