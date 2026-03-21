package com.springboot.projects.devvvotee.Dto.Auth;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String avatarUrl
) {
}
