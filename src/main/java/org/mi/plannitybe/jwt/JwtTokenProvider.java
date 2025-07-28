package org.mi.plannitybe.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.mi.plannitybe.exception.ExpiredTokenException;
import org.mi.plannitybe.exception.InvalidTokenException;
import org.mi.plannitybe.user.dto.CustomUserDetails;
import org.mi.plannitybe.user.type.UserRoleType;
import org.mi.plannitybe.user.type.UserStatusType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // User 정보로 AccessToken과 RefreshToken 생성하는 메소드
    public JwtToken generateToken(Authentication authentication) {


        // Authentication 객체에서 User의 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // FIXME) InvalidUserAuthorityException 예외 생성하여 바꾸기
        // 권한 또는 username이 없을 경우 예외 발생
        if (authorities.isEmpty() || authentication.getName().isEmpty()) {
            throw new RuntimeException("Invalid authentication");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        long now = (new Date()).getTime();

        // Access Token 생성
        long accessTokenValidity = 1000 * 60 * 15; // 15분
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .claim("userId", userDetails.getId())
                .claim("status", userDetails.getStatus().toString())
                .setExpiration(new Date(now + accessTokenValidity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token 생성
        long refreshTokenValidity = 1000L * 60 * 60 * 24 * 7; // 7일
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + refreshTokenValidity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메소드
    public Authentication getAuthentication(String accessToken) {
        // JWT 토큰 복호화하여 클레임 얻기
        Claims claims = getClaimsFromToken(accessToken);

        /*
        ** 토큰 생성 시 auth가 null인 경우 없도록 확인하고 토큰 서명으로 위변조 확인하므로 auth null 체크 불필요
        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }
         */

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();

        // UserDetails 객체 만들어서 Authentication return
//        UserDetails principal = new User(claims.getSubject(), "", authorities);
        UserDetails principal = CustomUserDetails.builder()
                .id(claims.get("userId", String.class))
                .email(claims.getSubject())
                .password("")
                .role(UserRoleType.valueOf(claims.get("auth", String.class)))
                .status(UserStatusType.valueOf(claims.get("status", String.class)))
                .build();
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // 토큰 정보를 검증하는 메소드
    public boolean validateToken(String token) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException("유효기간이 만료된 토큰입니다.");
        }

        /*
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty", e);
        }
        return false;
         */
    }
}