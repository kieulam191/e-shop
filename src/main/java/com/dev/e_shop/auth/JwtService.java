package com.dev.e_shop.auth;

import com.dev.e_shop.user.UserDetail;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    private final Clock clock;

    public JwtService(Clock clock) {
        this.clock = clock;
    }


    public String generateToken(UserDetail userDetail) {
        Instant now = clock.instant();
        Instant expiration = now.plus(1, ChronoUnit.HOURS);

        Map<String, Object> claims = createClaims(userDetail);

        return Jwts.builder()
                .subject(userDetail.getUsername())
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSecretKey())
                .compact();
    }


    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSecretKey())
                .clock((() -> Date.from(clock.instant())))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Map<String, Object>  createClaims(UserDetail userDetail) {
        Map<String, Object> claims = new HashMap<>();
        String roles = userDetail.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        claims.put("role", roles);

        return claims;
    }
}
