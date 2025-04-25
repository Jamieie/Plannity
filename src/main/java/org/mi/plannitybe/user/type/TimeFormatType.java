package org.mi.plannitybe.user.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mi.plannitybe.common.converter.CodeEnum;

@Getter
@RequiredArgsConstructor
public enum TimeFormatType implements CodeEnum {
    HOUR_12("HOUR_12", "12시간제"),
    HOUR_24("HOUR_24", "24시간제");
    
    private final String code;
    private final String description;
}