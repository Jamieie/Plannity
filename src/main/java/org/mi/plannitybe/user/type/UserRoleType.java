package org.mi.plannitybe.user.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mi.plannitybe.common.converter.CodeEnum;

@Getter
@RequiredArgsConstructor
public enum UserRoleType implements CodeEnum {
    ROLE_USER("USER", "일반 사용자"),
    ROLE_ADMIN("ADMIN", "관리자");
    
    private final String code;
    private final String description;
}