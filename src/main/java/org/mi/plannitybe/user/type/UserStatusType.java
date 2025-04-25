package org.mi.plannitybe.user.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mi.plannitybe.common.converter.CodeEnum;

@Getter
@RequiredArgsConstructor
public enum UserStatusType implements CodeEnum {
    ACTIVE("ACTIVE", "활성"),
    INACTIVE("INACTIVE", "비활성"),
    LOCKED("LOCKED", "잠김"),
    DELETED("DELETED", "삭제됨");
    
    private final String code;
    private final String description;
}