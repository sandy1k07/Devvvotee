package com.springboot.projects.devvvotee.Mapper;

import com.springboot.projects.devvvotee.Dto.Auth.SignupRequest;
import com.springboot.projects.devvvotee.Entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntityFromSignupRequest(SignupRequest signupRequest);
}
