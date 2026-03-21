package com.springboot.projects.devvvotee.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


import java.util.Set;

import static com.springboot.projects.devvvotee.enums.ProjectPermission.*;

@Getter
@RequiredArgsConstructor
public enum ProjectRole {
    EDITOR(VIEW, EDIT, DELETE, VIEW_MEMBERS),
    VIEWER(VIEW, VIEW_MEMBERS),
    OWNER(VIEW, EDIT, DELETE, MANAGE_MEMBERS, VIEW_MEMBERS);

    ProjectRole(ProjectPermission ...permissions) {
        this.permissions = Set.of(permissions);
    }

    private final Set<ProjectPermission> permissions;
}
