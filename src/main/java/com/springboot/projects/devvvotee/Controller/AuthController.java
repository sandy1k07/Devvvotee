package com.springboot.projects.devvvotee.Controller;


import com.springboot.projects.devvvotee.Dto.Auth.AuthResponse;
import com.springboot.projects.devvvotee.Dto.Auth.LoginRequest;
import com.springboot.projects.devvvotee.Dto.Auth.SignupRequest;
import com.springboot.projects.devvvotee.Dto.Auth.UserProfileResponse;
import com.springboot.projects.devvvotee.Service.AuthService;
import com.springboot.projects.devvvotee.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody @Valid SignupRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.signup(request, response));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(){
        Long userId = 1L;
        return ResponseEntity.ok(userService.getProfile(userId));
    }
}
