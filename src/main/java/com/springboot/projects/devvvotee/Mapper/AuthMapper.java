package com.springboot.projects.devvvotee.Mapper;

import com.springboot.projects.devvvotee.Dto.Auth.AuthResponse;
import com.springboot.projects.devvvotee.Dto.Auth.SignupRequest;
import com.springboot.projects.devvvotee.Dto.Auth.UserProfileResponse;
import com.springboot.projects.devvvotee.Entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    UserProfileResponse toUserProfileResponse(User user);
}
