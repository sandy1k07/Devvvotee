package com.springboot.projects.devvvotee.Dto.Project;

import com.springboot.projects.devvvotee.Entity.Project;
import com.springboot.projects.devvvotee.enums.ProjectRole;

public interface ProjectWithRole {
        Project getProject();
        ProjectRole getProjectRole();
}
