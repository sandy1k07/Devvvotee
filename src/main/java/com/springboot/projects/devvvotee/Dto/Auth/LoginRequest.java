package com.springboot.projects.devvvotee.Dto.Auth;

import jakarta.validation.constraints.*;

public record LoginRequest(

        @NotBlank
        String username,

        @Size(min = 8, max = 16)
        String password
) {
}
