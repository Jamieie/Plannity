package org.mi.plannitybe.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mi.plannitybe.exception.EmailAlreadyExistsException;
import org.mi.plannitybe.common.JwtToken;
import org.mi.plannitybe.user.dto.LoginRequest;
import org.mi.plannitybe.user.dto.SignUpRequest;
import org.mi.plannitybe.user.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입 api
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpRequest signUpRequest) {
        authService.signUp(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "회원가입이 완료되었습니다."
        ));
    }

    // 로그인 api
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        JwtToken jwtToken = authService.login(loginRequest);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", jwtToken.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.AUTHORIZATION, jwtToken.getGrantType() + " " + jwtToken.getAccessToken())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of(
                        "message", "로그인에 성공했습니다."
                ));
    }
}
