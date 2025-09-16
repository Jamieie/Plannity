package org.mi.plannitybe.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mi.plannitybe.user.dto.CustomUserDetails;
import org.mi.plannitybe.user.type.UserStatusType;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // 임의의 secret key (Base64로 인코딩된 256bit 이상)
        String secretKey = Base64.getEncoder().encodeToString("test-secret-key-test-secret-key-test-secret-key".getBytes());
        jwtTokenProvider = new JwtTokenProvider(secretKey);
    }

    // GenerateToken Test - User 정보를 Authentication 객체에 담아서 전달하면 JwtToken 객체 반환
    @Test
    void generateToken_ok() {
        // GIVEN - user 정보(email과 authorities) 담긴 Authentication 객체
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .email("test@test.com")
                .status(UserStatusType.ACTIVE)
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", authorities);

        // WHEN - generateToken 메서드 실행
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        // THEN - accessToken, RefreshToken 담긴 JwtToken 객체 반환
        assertNotNull(jwtToken);
        assertNotNull(jwtToken.getAccessToken());
        assertNotNull(jwtToken.getRefreshToken());
        assertEquals("Bearer", jwtToken.getGrantType());
    }
}