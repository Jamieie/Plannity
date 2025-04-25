package org.mi.plannitybe.user.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mi.plannitybe.common.converter.CodeEnum;

@Getter
@RequiredArgsConstructor
public enum WeekStartType implements CodeEnum {
    SUNDAY("SUNDAY", "일요일"),
    MONDAY("MONDAY", "월요일"),
    SATURDAY("SATURDAY", "토요일");
    
    private final String code;
    private final String description;
}