package com.springboot.projects.devvvotee.Dto.Auth;

// record means all the fields are immutable (privte and final), it will give us
// default getters, no / all args constructor, equals, hashcode

public record AuthResponse(
        String accessToken,
        UserProfileResponse user
) {
}
