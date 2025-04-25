package org.mi.plannitybe.schedule.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mi.plannitybe.common.converter.CodeEnum;

@Getter
@RequiredArgsConstructor
public enum TaskStatusType implements CodeEnum {
    NOT_STARTED("NOT_STARTED", "시작 전"),
    IN_PROGRESS("IN_PROGRESS", "진행 중"),
    COMPLETED("COMPLETED", "완료"),
    POSTPONED("POSTPONED", "연기됨"),
    CANCELED("CANCELED", "취소됨");
    
    private final String code;
    private final String description;
}