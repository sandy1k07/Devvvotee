package com.springboot.projects.devvvotee.Service;

import com.springboot.projects.devvvotee.Dto.Auth.UserProfileResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserProfileResponse getProfile(Long userId);
}
