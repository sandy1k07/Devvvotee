package com.springboot.projects.devvvotee.Service.Implementation;

import com.springboot.projects.devvvotee.Dto.Project.ProjectRequest;
import com.springboot.projects.devvvotee.Dto.Project.ProjectResponse;
import com.springboot.projects.devvvotee.Dto.Project.ProjectSummaryResponse;
import com.springboot.projects.devvvotee.Dto.Project.ProjectWithRole;
import com.springboot.projects.devvvotee.Entity.Project;
import com.springboot.projects.devvvotee.Entity.ProjectMember;
import com.springboot.projects.devvvotee.Entity.ProjectMemberId;
import com.springboot.projects.devvvotee.Entity.User;
import com.springboot.projects.devvvotee.ExceptionHandling.Exception.BadRequestException;
import com.springboot.projects.devvvotee.ExceptionHandling.Exception.ResourceNotFoundException;
import com.springboot.projects.devvvotee.Mapper.ProjectMapper;
import com.springboot.projects.devvvotee.Repository.ProjectMemberRepository;
import com.springboot.projects.devvvotee.Repository.ProjectRepository;
import com.springboot.projects.devvvotee.Repository.UserRepository;
import com.springboot.projects.devvvotee.Service.ProjectService;
import com.springboot.projects.devvvotee.Service.ProjectStarterTemplateService;
import com.springboot.projects.devvvotee.Service.SubscriptionService;
import com.springboot.projects.devvvotee.Utils.AuthUtil;
import com.springboot.projects.devvvotee.Utils.HelperFunctions;
import com.springboot.projects.devvvotee.enums.ProjectRole;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableMethodSecurity
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepository;
    ProjectMemberRepository projectMemberRepository;
    UserRepository userRepository;
    ProjectMapper projectMapper;
    HelperFunctions functions;
    SubscriptionService subscriptionService;
    ProjectStarterTemplateService projectStarterTemplateService;

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        Long userId = functions.getCurrentUserId();
        User owner = userRepository.getReferenceById(userId);

        if(!subscriptionService.canCreateNewProject()){
            throw new BadRequestException("Cannot create a new project with current plan, kindly upgrade your plan");
        }

        Project project = Project.builder()
                .name(request.name())
                .build();

        project = projectRepository.save(project);
        ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(), userId);
        ProjectMember projectMember = ProjectMember.builder()
                .id(projectMemberId)
                .project(project)
                .user(owner)
                .projectRole(ProjectRole.OWNER)
                .invitedAt(Instant.now())
                .AcceptedAt(Instant.now())
                .build();
        projectMemberRepository.save(projectMember);
        projectStarterTemplateService.initializeProjectWithTemplate(project.getId());
        return projectMapper.toProjectResponse(project, ProjectRole.OWNER);
    }

    @Override
    public List<ProjectResponse> getProjects() {
        Long userId = functions.getCurrentUserId();
        log.info("Requesting all projects for userId: {} from db", userId);
        List<ProjectResponse> projectResponseList =  projectRepository.findProjectsAccessibleByUser(userId).stream()
                    .map(projectWithRole -> projectMapper.toProjectResponse(
                            projectWithRole.getProject(), projectWithRole.getProjectRole()
                    )).toList();

        log.info("Returning " + projectResponseList.size() + " projects corresponding to userId: {}", userId);
        return projectResponseList;
    }

    @Override
    @PreAuthorize("@Security.canViewProject(#projectId)")
    public ProjectResponse getUserProjectById(Long projectId) {
        Long userId = functions.getCurrentUserId();
        ProjectWithRole projectWithRole = functions.getAccessibleProjectWithRoleById(projectId,  userId);
        return projectMapper.toProjectResponse(projectWithRole.getProject(), projectWithRole.getProjectRole());
    }

    @Override
    @PreAuthorize("@Security.canEditProject(#projectId)")
    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        Long userId = functions.getCurrentUserId();
        Project project = functions.getAccessibleProjectById(projectId, userId);
        ProjectWithRole projectWithRole = functions.getAccessibleProjectWithRoleById(projectId,  userId);
        project.setName(request.name());
        return projectMapper.toProjectResponse(projectRepository.save(project), null);
    }

    @Override
    @PreAuthorize("@Security.canDeleteProject(#projectId)")
    public void softDelete(Long projectId) {
        Long userId = functions.getCurrentUserId();
        Project project = functions.getAccessibleProjectById(projectId, userId);
        project.setDeletedAt(Instant.now());
        projectRepository.save(project);
    }

}
