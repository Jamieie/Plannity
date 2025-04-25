package org.mi.plannitybe.user.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mi.plannitybe.common.converter.CodeEnum;

@Getter
@RequiredArgsConstructor
public enum SocialProviderType implements CodeEnum {
    GOOGLE("GOOGLE", "구글"),
    KAKAO("KAKAO", "카카오"),
    NAVER("NAVER", "네이버"),
    APPLE("APPLE", "애플");
    
    private final String code;
    private final String description;
}