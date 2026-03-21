package com.springboot.projects.devvvotee.Utils;

import com.springboot.projects.devvvotee.Entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthUtil {

    @Value("${jwt.secret-key}")
    String secret_key;

    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(secret_key.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user){
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000*60*2))
                .signWith(getSecretKey())
                .compact();

    }

    public String generateRefreshToken(User user){
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000*60*10))
                .signWith(getSecretKey())
                .compact();

    }

    public String generateAccessTokenFromJwtPrincipal(JwtUserPrincipal user){
        return Jwts.builder()
                .subject(user.userId().toString())
                .claim("email", user.email())
                .claim("username", user.username())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000*60*2))
                .signWith(getSecretKey())
                .compact();
    }

    public JwtUserPrincipal getJwtUserPrincipal(String token){
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String email = (String) claims.get("email");
        String username = (String) claims.get("username");
        Long id = Long.parseLong(claims.getSubject());
        return new JwtUserPrincipal(email, username, id, List.of());
    }
}
