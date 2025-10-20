package org.mi.plannitybe.user.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mi.plannitybe.exception.EmailAlreadyExistsException;
import org.mi.plannitybe.user.dto.SignUpRequest;
import org.mi.plannitybe.user.entity.User;
import org.mi.plannitybe.user.repository.UserRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공")
    void signUpOk() {
        // GIVEN - 중복되지 않은 이메일과 비밀번호를 담은 객체
        String okEmail = "test@test.com";
        String password = "test1234@";

        // WHEN - 회원가입 메소드 호출
        SignUpRequest signUpRequest = new SignUpRequest(okEmail, password);
        authService.signUp(signUpRequest);

        // THEN - userRepository.save() 함수 호출
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    void signUpFail_emailAlreadyExist() {
        // GIVEN - 이미 존재하는 이메일과 비밀번호를 담은 객체
        String failEmail = "test@test.com";
        String password = "test1234@";
        given(userRepository.save(any(User.class))).willThrow(DataIntegrityViolationException.class);

        // WHEN & THEN - 회원가입 메소드 호출하면 예외 발생
        SignUpRequest signUpRequest = new SignUpRequest(failEmail, password);
        assertThrows(EmailAlreadyExistsException.class, () -> authService.signUp(signUpRequest));
    }
}