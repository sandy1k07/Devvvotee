package com.springboot.projects.devvvotee.Security;

import com.springboot.projects.devvvotee.Repository.ProjectMemberRepository;
import com.springboot.projects.devvvotee.Utils.HelperFunctions;
import com.springboot.projects.devvvotee.enums.ProjectPermission;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("Security")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityExpression {
    ProjectMemberRepository projectMemberRepository;
    HelperFunctions functions;

    public boolean canViewProject(Long projectId){
        return hasPermission(projectId, ProjectPermission.VIEW);
    }

    public boolean canEditProject(Long projectId){
        return hasPermission(projectId, ProjectPermission.EDIT);
    }

    public boolean canDeleteProject(Long projectId){
        return hasPermission(projectId, ProjectPermission.DELETE);
    }

    public boolean canViewMembers(Long projectId){
        return hasPermission(projectId, ProjectPermission.VIEW_MEMBERS);
    }

    public boolean canManageMembers(Long projectId){
        return hasPermission(projectId, ProjectPermission.MANAGE_MEMBERS);
    }

    // Helper methods
    public boolean hasPermission(Long projectId, ProjectPermission permission) {
        Long userId = functions.getCurrentUserId();
        log.info("Checking permission: user={} project={} permission={}",
                userId, projectId, permission);
        return projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId)
                .map(role -> role.getPermissions().contains(permission))
                .orElse(false);
    }
}
