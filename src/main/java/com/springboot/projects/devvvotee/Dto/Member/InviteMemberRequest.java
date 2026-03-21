package com.springboot.projects.devvvotee.Dto.Member;

import com.springboot.projects.devvvotee.enums.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequest(
        @NotBlank
        String username,

        @NotNull
        ProjectRole role
) {
}
