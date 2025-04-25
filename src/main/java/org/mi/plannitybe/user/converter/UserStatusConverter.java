package org.mi.plannitybe.user.converter;

import jakarta.persistence.Converter;
import org.mi.plannitybe.common.converter.CodeConverter;
import org.mi.plannitybe.user.type.UserStatusType;

@Converter(autoApply = true)
public class UserStatusConverter extends CodeConverter<UserStatusType> {

    public UserStatusConverter() {
        super(UserStatusType.class, UserStatusType::getCode, true);
    }
}