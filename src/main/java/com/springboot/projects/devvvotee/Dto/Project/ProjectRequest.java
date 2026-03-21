package com.springboot.projects.devvvotee.Dto.Project;

import jakarta.validation.constraints.NotBlank;

public record ProjectRequest(
        @NotBlank
        String name
) {
}
