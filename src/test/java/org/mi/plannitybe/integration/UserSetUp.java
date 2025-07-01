package org.mi.plannitybe.integration;

import org.mi.plannitybe.user.entity.User;
import org.mi.plannitybe.user.repository.UserRepository;
import org.mi.plannitybe.user.type.UserRoleType;
import org.mi.plannitybe.user.type.UserStatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserSetUp {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String saveUser(String email, String pwd) {

        String uuid = UUID.randomUUID().toString().replace("-", "");    // UUID 생성

        User user = User.builder()
                .id(uuid)
                .email(email)
                .pwd(passwordEncoder.encode(pwd))
                .role(UserRoleType.ROLE_USER)
                .status(UserStatusType.ACTIVE)
                .build();

        return userRepository.save(user).getId();
    }

    public String saveInactiveUser(String email, String pwd) {

        String uuid = UUID.randomUUID().toString().replace("-", "");    // UUID 생성

        User user = User.builder()
                .id(uuid)
                .email(email)
                .pwd(passwordEncoder.encode(pwd))
                .role(UserRoleType.ROLE_USER)
                .status(UserStatusType.INACTIVE)
                .build();

        return userRepository.save(user).getId();
    }
}
