package com.springboot.projects.devvvotee.Mapper;

import com.springboot.projects.devvvotee.Dto.Member.MemberResponse;
import com.springboot.projects.devvvotee.Entity.ProjectMember;
import com.springboot.projects.devvvotee.Entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "role", constant = "OWNER")
    MemberResponse toMemberResponseFromUser(User user);

    @Mapping(target = "role", source = "projectRole")
    @Mapping(target = "username", source="user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "userId", source = "user.id")
    MemberResponse toMemberResponseFromMember(ProjectMember member);
}
