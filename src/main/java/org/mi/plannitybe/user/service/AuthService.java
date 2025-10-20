package org.mi.plannitybe.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mi.plannitybe.exception.EmailAlreadyExistsException;
import org.mi.plannitybe.jwt.JwtToken;
import org.mi.plannitybe.jwt.JwtTokenProvider;
import org.mi.plannitybe.schedule.entity.EventList;
import org.mi.plannitybe.user.dto.LoginRequest;
import org.mi.plannitybe.user.dto.SignUpRequest;
import org.mi.plannitybe.user.entity.User;
import org.mi.plannitybe.user.repository.UserRepository;
import org.mi.plannitybe.user.type.UserRoleType;
import org.mi.plannitybe.user.type.UserStatusType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입 메소드
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.DEFAULT)
    public void signUp(SignUpRequest signUpRequest) {

        // User 객체 생성 후 디비 저장 - UUID 생성, 비밀번호 암호화, 역할 부여, 계정상태 설정, 회원가입일시 설정
        String encodedPwd = passwordEncoder.encode(signUpRequest.getPwd().trim());      // 비밀번호 암호화
        LocalDateTime registerDate = LocalDateTime.now();   // 회원 가입 일시 및 약관 동의 일시

        User user = User.builder()
                .email(signUpRequest.getEmail())
                .pwd(encodedPwd)
                .role(UserRoleType.ROLE_USER)
                .status(UserStatusType.ACTIVE)
                .registeredAt(registerDate)
                .build();

        // Default EventList 생성
        EventList eventList = EventList.builder()
                .user(user)
                .name("inbox")
                .isDefault(true)
                .build();
        user.addEventList(eventList);

        // TODO) 약관동의내역 DB 저장

        // User 저장 시도할 때 Email 중복 시 예외 발생
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.error("User signup failed due to data integrity violation - email: {}, message: {}, cause: {}",
                    signUpRequest.getEmail(), e.getMessage(),
                    e.getCause() != null ? e.getCause().getClass().getSimpleName() : "null", e);
            throw new EmailAlreadyExistsException(signUpRequest.getEmail());
        }
    }

    @Transactional(readOnly = true)
    public JwtToken login(LoginRequest loginRequest) {
        // email + pwd 기반으로 Authentication 객체 생성
        // 이때 authentication은 인증 여부를 확인하는 authenticated 값이 false
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPwd());

        // email & pwd 검증 - authenticate() 메서드를 통해 요청된 User에 대한 검증 진행
        // authenticate() 메서드가 실행될 때 CustomUserDetailsService에서 구현한 loadUserByUsername 메서드 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 인증 정보를 기반으로 JWT 토큰 생성하여 반환
        return jwtTokenProvider.generateToken(authentication);
    }
}
