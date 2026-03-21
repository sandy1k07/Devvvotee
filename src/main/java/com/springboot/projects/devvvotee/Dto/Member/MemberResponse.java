package com.springboot.projects.devvvotee.Dto.Member;

import com.springboot.projects.devvvotee.enums.ProjectRole;

import java.time.Instant;

public record MemberResponse(
        Long userId,
        String email,
        String username,
        String avatarUrl,
        ProjectRole role,
        Instant invitedAt
) {
}
