package org.mi.plannitybe.user.converter;

import jakarta.persistence.Converter;
import org.mi.plannitybe.common.converter.CodeConverter;
import org.mi.plannitybe.user.type.UserRoleType;

@Converter(autoApply = true)
public class UserRoleConverter extends CodeConverter<UserRoleType> {

    public UserRoleConverter() {
        super(UserRoleType.class, UserRoleType::getCode, true);
    }
}