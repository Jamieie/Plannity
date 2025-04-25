package org.mi.plannitybe.user.converter;

import jakarta.persistence.Converter;
import org.mi.plannitybe.common.converter.CodeConverter;
import org.mi.plannitybe.user.type.SocialProviderType;

@Converter(autoApply = true)
public class SocialProviderConverter extends CodeConverter<SocialProviderType> {

    public SocialProviderConverter() {
        super(SocialProviderType.class, SocialProviderType::getCode, true);
    }
}