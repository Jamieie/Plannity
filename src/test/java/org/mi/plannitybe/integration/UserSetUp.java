package org.mi.plannitybe.integration;

import org.mi.plannitybe.user.entity.User;
import org.mi.plannitybe.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserSetUp {

    @Autowired
    private UserRepository userRepository;

    public String saveUser(String email, String pwd) {

        String uuid = UUID.randomUUID().toString().replace("-", "");    // UUID 생성

        User user = User.builder()
                .id(uuid)
                .email(email)
                .pwd(pwd)
                .build();

        return userRepository.save(user).getId();
    }
}
