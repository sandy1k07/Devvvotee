package com.springboot.projects.devvvotee.Service;

import com.springboot.projects.devvvotee.Dto.Auth.AuthResponse;
import com.springboot.projects.devvvotee.Dto.Auth.LoginRequest;
import com.springboot.projects.devvvotee.Dto.Auth.SignupRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;

public interface AuthService {
    AuthResponse signup(SignupRequest request, HttpServletResponse response);

    AuthResponse login(LoginRequest request, HttpServletResponse response);
}
