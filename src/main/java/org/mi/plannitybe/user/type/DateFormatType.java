package org.mi.plannitybe.user.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.mi.plannitybe.common.converter.CodeEnum;

@Getter
@RequiredArgsConstructor
public enum DateFormatType implements CodeEnum {
    YYYY_MM_DD("YYYY_MM_DD", "YYYY-MM-DD"),
    MM_DD_YYYY("MM_DD_YYYY", "MM-DD-YYYY"),
    DD_MM_YYYY("DD_MM_YYYY", "DD-MM-YYYY");
    
    private final String code;
    private final String description;
}