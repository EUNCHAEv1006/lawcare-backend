package com.lawcare.lawcarebackend.common.security;

import com.lawcare.lawcarebackend.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Component
public class TokenProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private Key key;

    private static final long EXPIRATION_TIME_MS = 60 * 60 * 1000; // 1시간

    /**
     * Bean 생성 후 실행되는 초기화 메서드
     * - secretKeyString(문자열)을 바이트로 디코딩하여 HMAC-SHA 키 객체를 생성
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = secretKey.getBytes();
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWT 토큰 생성
     *
     * @param userId 사용자 식별값
     * @param role   Role.USER or Role.COUNSELOR
     * @return 생성된 JWT
     */
    public String createToken(Long userId, Role role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME_MS);

        return Jwts.builder()
                   .setSubject(String.valueOf(userId))
                   .setIssuedAt(now)
                   .setExpiration(expiryDate)
                   .claim("role", role.name())
                   .signWith(key, SignatureAlgorithm.HS512)
                   .compact();
    }

    /**
     * 토큰 유효성 검증
     *
     * @param token 클라이언트가 보낸 JWT
     * @return true: 유효, false: 무효
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰에서 인증 객체를 추출하여 반환
     * - Subject(= userId)와 role 값을 꺼내 스프링 시큐리티 Authentication 객체로 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

        String userId = claims.getSubject();
        String roleName = claims.get("role", String.class);

        String authorityName = "ROLE_" + roleName;
        var authorities = Collections.singletonList(new SimpleGrantedAuthority(authorityName));

        return new UsernamePasswordAuthenticationToken(
            userId,
            "",
            authorities
        );
    }
}
