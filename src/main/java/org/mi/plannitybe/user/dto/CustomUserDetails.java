package org.mi.plannitybe.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.mi.plannitybe.user.type.UserRoleType;
import org.mi.plannitybe.user.type.UserStatusType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@Builder
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final String id;
    private final String username;
    private final String email;
    private final String password;
    private final UserRoleType role;
    private final UserStatusType status;

    // claims.getName()시 email을 반환하도록 설정
    @Override
    public String getUsername() {
        return email;
    }

    // 사용자 권한
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.toString()));
    }

    // 계정 활성화 여부
    @Override
    public boolean isEnabled() {
        return status == UserStatusType.ACTIVE;
    }

    // 계정의 만료 여부
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    // 계정의 잠금 여부
    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    // 자격 증명의 만료 여부
    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }
}
