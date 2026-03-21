package com.springboot.projects.devvvotee.Utils;

import com.springboot.projects.devvvotee.Entity.Project;
import com.springboot.projects.devvvotee.Entity.ProjectMember;
import com.springboot.projects.devvvotee.Entity.ProjectMemberId;
import com.springboot.projects.devvvotee.Entity.User;
import com.springboot.projects.devvvotee.ExceptionHandling.Exception.ResourceNotFoundException;
import com.springboot.projects.devvvotee.Repository.ProjectMemberRepository;
import com.springboot.projects.devvvotee.Repository.ProjectRepository;
import com.springboot.projects.devvvotee.Repository.UserRepository;
import io.jsonwebtoken.Jwt;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class HelperFunctions {

    ProjectRepository projectRepository;
    ProjectMemberRepository projectMemberRepository;
    UserRepository userRepository;

    public Project getAccessibleProjectById(Long projectId, Long userId) {
        return projectRepository.findAccessibleProjectById(projectId, userId).orElseThrow(
                () -> new ResourceNotFoundException("Project ", projectId.toString())
        );
    }

    public ProjectMember getProjectMemberById(ProjectMemberId projectMemberId){
        return projectMemberRepository.findById(projectMemberId).orElseThrow(
                () -> new ResourceNotFoundException("Project member ", projectMemberId.getUserId().toString())
        );
    }

    // only called when project exists
    public Project getProject(Long projectId) {
        return projectRepository.getReferenceById(projectId);
    }

    // only called when user exists
    public User getUser(Long userId) {
        return userRepository.getReferenceById(userId);
    }

    public Long getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal)){
            throw new AuthenticationCredentialsNotFoundException("Not Authenticated");
        }
        JwtUserPrincipal jwtUserPrincipal = (JwtUserPrincipal) authentication.getPrincipal();
        return jwtUserPrincipal.userId();
    }

    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User ", userId.toString())
        );
    }

    public ResponseCookie CookieBuilder(String cookieName, String cookieValue){
        return ResponseCookie.from(cookieName, cookieValue)
                .path("/")
                .httpOnly(true)
                .build();
    }

    public void addCookieToResponse(ResponseCookie responseCookie, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }
}
