package org.mi.plannitybe.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mi.plannitybe.common.JwtToken;
import org.mi.plannitybe.exception.InvalidTokenException;
import org.mi.plannitybe.user.dto.CustomUserDetails;
import org.mi.plannitybe.user.type.UserRoleType;
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
    private Authentication validAuthentication;

    @BeforeEach
    void setUp() {
        // 임의의 secret key (Base64로 인코딩된 256bit 이상)
        String secretKey = Base64.getEncoder().encodeToString("test-secret-key-test-secret-key-test-secret-key".getBytes());
        jwtTokenProvider = new JwtTokenProvider(secretKey);
        
        // 테스트용 Authentication 객체 생성
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id("user123")
                .email("test@test.com")
                .role(UserRoleType.ROLE_USER)
                .status(UserStatusType.ACTIVE)
                .build();
        validAuthentication = new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    // ================ generateToken 테스트 ================

    @Test
    void generateToken_success() {
        // WHEN
        JwtToken jwtToken = jwtTokenProvider.generateToken(validAuthentication);

        // THEN
        assertNotNull(jwtToken);
        assertNotNull(jwtToken.getAccessToken());
        assertNotNull(jwtToken.getRefreshToken());
        assertEquals("Bearer", jwtToken.getGrantType());
    }

    @Test
    void generateToken_fail_emptyAuthorities() {
        // GIVEN - 권한이 없는 Authentication
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id("user123")
                .email("test@test.com")
                .role(UserRoleType.ROLE_USER)
                .status(UserStatusType.ACTIVE)
                .build();
        Authentication authWithoutAuthorities = new UsernamePasswordAuthenticationToken(userDetails, "", List.of());

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jwtTokenProvider.generateToken(authWithoutAuthorities));
        assertEquals("Invalid authentication", exception.getMessage());
    }

    @Test
    void generateToken_fail_emptyName() {
        // GIVEN - 이름이 없는 Authentication
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id("user123")
                .email("")  // 빈 이메일
                .role(UserRoleType.ROLE_USER)
                .status(UserStatusType.ACTIVE)
                .build();
        Authentication authWithoutName = new UsernamePasswordAuthenticationToken(userDetails, "", authorities);

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jwtTokenProvider.generateToken(authWithoutName));
        assertEquals("Invalid authentication", exception.getMessage());
    }

    // ================ getAuthentication 테스트 ================

    @Test
    void getAuthentication_success() {
        // GIVEN - 유효한 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(validAuthentication);

        // WHEN
        Authentication authentication = jwtTokenProvider.getAuthentication(jwtToken.getAccessToken());

        // THEN
        assertNotNull(authentication);
        assertEquals("test@test.com", authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        assertEquals("user123", userDetails.getId());
        assertEquals("test@test.com", userDetails.getEmail());
        assertEquals(UserStatusType.ACTIVE, userDetails.getStatus());
    }

    // ================ validateToken 테스트 ================

    @Test
    void validateToken_success() {
        // GIVEN - 유효한 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(validAuthentication);

        // WHEN & THEN
        assertTrue(jwtTokenProvider.validateToken(jwtToken.getAccessToken()));
        assertTrue(jwtTokenProvider.validateToken(jwtToken.getRefreshToken()));
    }

    @Test
    void validateToken_fail_invalidTokenFormats() {
        // GIVEN - 다양한 잘못된 토큰 형식
        String[] invalidTokens = {
            "invalid.jwt.token",
            "not-a-jwt-token", 
            "",
            null
        };

        // WHEN & THEN
        for (String token : invalidTokens) {
            InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                    () -> jwtTokenProvider.validateToken(token));
            assertEquals("유효하지 않은 토큰입니다.", exception.getMessage());
        }
    }
}