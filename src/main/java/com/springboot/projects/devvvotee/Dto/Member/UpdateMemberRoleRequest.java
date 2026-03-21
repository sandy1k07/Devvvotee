package com.springboot.projects.devvvotee.Dto.Member;

import com.springboot.projects.devvvotee.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
        @NotNull
        ProjectRole role
) {
}
