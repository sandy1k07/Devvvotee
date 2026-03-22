package com.springboot.projects.devvvotee.Filter;

import com.springboot.projects.devvvotee.Utils.AuthUtil;
import com.springboot.projects.devvvotee.Utils.HelperFunctions;
import com.springboot.projects.devvvotee.Utils.JwtUserPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;


@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AuthUtil authUtil;
    private final HelperFunctions functions;
    private final HandlerExceptionResolver exceptionResolver;

    public JwtAuthFilter(
            AuthUtil authUtil,
            HelperFunctions functions,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.authUtil = authUtil;
        this.functions = functions;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        return request.getRequestURI().startsWith("/api/auth/") || request.getRequestURI().startsWith("/webhooks/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Incoming req in JwtAuthFilter: {}", request.getRequestURI());

        try {
            if (request.getCookies() == null) {
                log.error("Cookies are empty");
                filterChain.doFilter(request, response);
                return;
            }
            String accessToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> "accessToken".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue).orElseThrow(
                            () -> new AuthenticationServiceException("Cookie not found")
                    );
            String refreshToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> "refreshToken".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue).orElseThrow(
                            () -> new AuthenticationServiceException("Cookie not found")
                    );
            try {
                JwtUserPrincipal user = authUtil.getJwtUserPrincipal(accessToken);
                if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            user, null, user.authorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
                if(user != null) log.info("Access token verified for userID: {}", user.userId());
                filterChain.doFilter(request, response);
            } catch (ExpiredJwtException e) {
                try {
                    log.info("access token expired");
                    JwtUserPrincipal user = authUtil.getJwtUserPrincipal(refreshToken);
                    String newAccessToken = authUtil.generateAccessTokenFromJwtPrincipal(user);
                    if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                user, null, user.authorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                    functions.addCookieToResponse(functions.CookieBuilder("accessToken", newAccessToken), response);
                    if(user != null) log.info("Refresh token verified for userID: {}", user.userId());
                    filterChain.doFilter(request, response);
                } catch (ExpiredJwtException ex) {
                    log.error("Both tokens expired");
                    exceptionResolver.resolveException(request, response, null, ex);
                }
            }
        } catch (Exception e) {
            exceptionResolver.resolveException(request, response, null, e);
        }
    }
}
