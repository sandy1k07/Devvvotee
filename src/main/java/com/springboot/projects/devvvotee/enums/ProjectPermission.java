package com.springboot.projects.devvvotee.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectPermission {
    VIEW("project:view"),
    EDIT("project:edit"),
    DELETE("project:delete"),
    MANAGE_MEMBERS("project_member:manage_members"),
    VIEW_MEMBERS("project_member:view_members");

    private final String value;
}
