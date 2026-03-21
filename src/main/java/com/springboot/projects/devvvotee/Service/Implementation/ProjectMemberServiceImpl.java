package com.springboot.projects.devvvotee.Service.Implementation;

import com.springboot.projects.devvvotee.Dto.Member.InviteMemberRequest;
import com.springboot.projects.devvvotee.Dto.Member.MemberResponse;
import com.springboot.projects.devvvotee.Dto.Member.UpdateMemberRoleRequest;
import com.springboot.projects.devvvotee.Entity.Project;
import com.springboot.projects.devvvotee.Entity.ProjectMember;
import com.springboot.projects.devvvotee.Entity.ProjectMemberId;
import com.springboot.projects.devvvotee.Entity.User;
import com.springboot.projects.devvvotee.Mapper.ProjectMemberMapper;
import com.springboot.projects.devvvotee.Repository.ProjectMemberRepository;
import com.springboot.projects.devvvotee.Repository.UserRepository;
import com.springboot.projects.devvvotee.Service.ProjectMemberService;
import com.springboot.projects.devvvotee.Utils.HelperFunctions;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class ProjectMemberServiceImpl implements ProjectMemberService {

    ProjectMemberRepository projectMemberRepository;
    UserRepository userRepository;
    ProjectMemberMapper projectMemberMapper;
    HelperFunctions functions;


    @Override
    @PreAuthorize("@Security.canViewMembers(#projectId)")
    public List<MemberResponse> getProjectMembers(Long projectId) {
        Long userId = functions.getCurrentUserId();
        Project project = functions.getAccessibleProjectById(projectId, userId);

        return new ArrayList<>(projectMemberRepository.findByIdProjectId(projectId)
                .stream().map(projectMemberMapper::toMemberResponseFromMember)
                .toList());
    }

    @Override
    @PreAuthorize("@Security.canManageMembers(#projectId)")
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request) {
        Long userId = functions.getCurrentUserId();
        Project project = functions.getAccessibleProjectById(projectId, userId);
        User invitee = userRepository.findByUsername(request.username()).orElseThrow();
        if(invitee.getId().equals(userId)){
            throw new RuntimeException("Cannot invite yourself");
        }
//        log.info("invitee id: " + invitee.getId());

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, invitee.getId());
        if(projectMemberRepository.existsById(projectMemberId)){
            throw new RuntimeException("Cannot invite an already existing member");
        }

        ProjectMember projectMember = ProjectMember.builder()
                .id(projectMemberId)
                .user(invitee)
                .projectRole(request.role())
                .project(project)
                .invitedAt(Instant.now())
                .build();
        projectMemberRepository.save(projectMember);
        return projectMemberMapper.toMemberResponseFromMember(projectMember);
    }

    @Override
    @PreAuthorize("@Security.canManageMembers(#projectId)")
    public MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request) {
        Long userId = functions.getCurrentUserId();
        Project project = functions.getAccessibleProjectById(projectId, userId);
        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);

        ProjectMember projectMember = functions.getProjectMemberById(projectMemberId);

        projectMember.setProjectRole(request.role());
        projectMemberRepository.save(projectMember);
        return projectMemberMapper.toMemberResponseFromMember(projectMember);
    }

    @Override
    @PreAuthorize("@Security.canManageMembers(#projectId)")
    public void removeProjectMember(Long projectId, Long memberId) {
        Long userId = functions.getCurrentUserId();
        Project project = functions.getAccessibleProjectById(projectId, userId);
        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        ProjectMember projectMember = functions.getProjectMemberById(projectMemberId);
        projectMemberRepository.delete(projectMember);
    }


}
