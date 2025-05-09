package org.mi.plannitybe.user.service;

import lombok.RequiredArgsConstructor;
import org.mi.plannitybe.exception.EmailAlreadyExistsException;
import org.mi.plannitybe.user.dto.SignUpRequest;
import org.mi.plannitybe.user.entity.User;
import org.mi.plannitybe.user.repository.UserRepository;
import org.mi.plannitybe.user.type.UserRoleType;
import org.mi.plannitybe.user.type.UserStatusType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 메소드
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.DEFAULT)
    public void signUp(SignUpRequest signUpRequest) {

        // 이메일 중복 검사 - 이미 존재하면 예외 발생
        String email = signUpRequest.getEmail();
        if (emailExists(email)) {
            throw new EmailAlreadyExistsException("이미 가입된 이메일입니다.");
        }

        // User 객체 생성 후 디비 저장 - UUID 생성, 비밀번호 암호화, 역할 부여, 계정상태 설정, 회원가입일시 설정
        String uuid = UUID.randomUUID().toString().replace("-", "");    // UUID 생성
        String encodedPwd = passwordEncoder.encode(signUpRequest.getPwd().trim());      // 비밀번호 암호화

        User user = User.builder()
                .id(uuid)
                .email(email)
                .pwd(encodedPwd)
                .role(UserRoleType.USER)
                .status(UserStatusType.ACTIVE)
                .registeredAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        // TODO) 약관동의내역 DB 저장
    }

    // 이메일 중복 검사
    private boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
