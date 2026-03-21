package com.springboot.projects.devvvotee.Utils;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public record JwtUserPrincipal(
        String username,
        String email,
        Long userId,
        List<GrantedAuthority> authorities
) {
}
