package com.springboot.projects.devvvotee.Dto.Project;

import com.springboot.projects.devvvotee.Dto.Auth.UserProfileResponse;

import java.time.Instant;

public record ProjectResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        UserProfileResponse owner
) {
}
