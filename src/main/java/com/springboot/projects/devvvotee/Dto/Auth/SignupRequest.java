package com.springboot.projects.devvvotee.Dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @Size(min = 8, max = 16)
        String username,

        @Email @NotBlank
        String email,

        @Size(min = 8, max = 16)
        String password
) {
}
