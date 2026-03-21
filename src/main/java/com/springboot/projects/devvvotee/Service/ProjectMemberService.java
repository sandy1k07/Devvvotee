package com.springboot.projects.devvvotee.Service;

import com.springboot.projects.devvvotee.Dto.Member.InviteMemberRequest;
import com.springboot.projects.devvvotee.Dto.Member.MemberResponse;
import com.springboot.projects.devvvotee.Dto.Member.UpdateMemberRoleRequest;
import com.springboot.projects.devvvotee.Entity.ProjectMember;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ProjectMemberService {
    List<MemberResponse> getProjectMembers(Long projectId);

    MemberResponse inviteMember(Long projectId, InviteMemberRequest request);

    MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request);

    void removeProjectMember(Long projectId, Long memberId);
}
