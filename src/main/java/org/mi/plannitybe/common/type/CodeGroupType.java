package org.mi.plannitybe.common.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mi.plannitybe.common.converter.CodeEnum;

@Getter
@RequiredArgsConstructor
public enum CodeGroupType implements CodeEnum {
    USER_ROLE("USER_ROLE", "사용자 역할"),
    USER_STATUS("USER_STATUS", "사용자 상태"),
    SOCIAL_PROVIDER("SOCIAL_PROVIDER", "소셜 제공자"),
    DATE_FORMAT("DATE_FORMAT", "날짜 형식"),
    TIME_FORMAT("TIME_FORMAT", "시간 형식"),
    TIME_ZONE("TIME_ZONE", "시간대"),
    WEEK_START("WEEK_START", "주 시작일"),
    TASK_STATUS("TASK_STATUS", "작업 상태");
    
    private final String code;
    private final String description;
}