package com.springboot.projects.devvvotee.Service.Implementation;

import com.springboot.projects.devvvotee.Dto.Auth.AuthResponse;
import com.springboot.projects.devvvotee.Dto.Auth.LoginRequest;
import com.springboot.projects.devvvotee.Dto.Auth.SignupRequest;
import com.springboot.projects.devvvotee.Entity.User;
import com.springboot.projects.devvvotee.ExceptionHandling.Exception.BadRequestException;
import com.springboot.projects.devvvotee.Mapper.AuthMapper;
import com.springboot.projects.devvvotee.Mapper.UserMapper;
import com.springboot.projects.devvvotee.Repository.UserRepository;
import com.springboot.projects.devvvotee.Service.AuthService;
import com.springboot.projects.devvvotee.Utils.AuthUtil;
import com.springboot.projects.devvvotee.Utils.HelperFunctions;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    AuthMapper authMapper;
    AuthUtil authUtil;
    AuthenticationManager authenticationManager;
    HelperFunctions functions;

    @Override
    public AuthResponse signup(SignupRequest request, HttpServletResponse response) {
        Optional<User> user = userRepository.findByUsernameOrEmail(request.username(), request.email());
        user.ifPresent(u -> {
            throw new BadRequestException("Username or Email already exists");
        });

        User signedUpUser = userMapper.toEntityFromSignupRequest(request);
        signedUpUser.setPassword(passwordEncoder.encode(signedUpUser.getPassword()));
        userRepository.save(signedUpUser);
        String accessToken = authUtil.generateAccessToken(signedUpUser);
        String refreshToken = authUtil.generateRefreshToken(signedUpUser);
        functions.addCookieToResponse(functions.CookieBuilder("accessToken", accessToken), response);
        functions.addCookieToResponse(functions.CookieBuilder("refreshToken", refreshToken), response);
        return new AuthResponse(accessToken, authMapper.toUserProfileResponse(signedUpUser));
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user =  (User) authentication.getPrincipal();
        String accessToken = authUtil.generateAccessToken(user);
        String refreshToken = authUtil.generateRefreshToken(user);
        functions.addCookieToResponse(functions.CookieBuilder("accessToken", accessToken), response);
        functions.addCookieToResponse(functions.CookieBuilder("refreshToken", refreshToken), response);
        return new  AuthResponse(accessToken, authMapper.toUserProfileResponse(user));
    }

    public void generateAndAddCookies(User user, HttpServletResponse response) {
        String accessToken = authUtil.generateAccessToken(user);
        String refreshToken = authUtil.generateRefreshToken(user);
        functions.addCookieToResponse(functions.CookieBuilder("accessToken", accessToken), response);
        functions.addCookieToResponse(functions.CookieBuilder("refreshToken", refreshToken), response);
    }
}
