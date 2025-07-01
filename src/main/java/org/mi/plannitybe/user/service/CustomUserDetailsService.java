package org.mi.plannitybe.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mi.plannitybe.user.dto.CustomUserDetails;
import org.mi.plannitybe.user.entity.User;
import org.mi.plannitybe.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        return CustomUserDetails.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPwd())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    };
}
