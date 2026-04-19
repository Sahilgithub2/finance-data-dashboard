package com.finance.security;

import com.finance.config.AppJwtProperties;
import com.finance.enums.Role;
import com.finance.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final AppJwtProperties properties;

    public JwtService(AppJwtProperties properties) {
        this.properties = properties;
    }

    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        Date issued = new Date(now);
        Date expiry = new Date(now + properties.getExpirationMs());
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(issued)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    public AuthUserDetails parseToken(String token) {
        Claims claims =
                Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
        Long userId = claims.get("userId", Long.class);
        String roleName = claims.get("role", String.class);
        Role role = Role.valueOf(roleName);
        String email = claims.getSubject();
        return new AuthUserDetails(userId, email, "", role, true);
    }

    private SecretKey signingKey() {
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
