package org.mi.plannitybe.common.converter;

import jakarta.persistence.AttributeConverter;

import java.util.Arrays;
import java.util.function.Function;

/**
 * enum과 데이터베이스 컬럼 간의 변환을 위한 추상 컨버터 클래스
 */
public abstract class CodeConverter<E extends Enum<E> & CodeEnum> implements AttributeConverter<E, String> {

    private final Class<E> enumClass;
    private final Function<E, String> codeGetter;
    private final boolean nullable;

    /**
     * enum 필드를 데이터베이스 컬럼으로 변환하는 컨버터 생성자
     * 
     * @param enumClass enum 클래스
     * @param codeGetter enum에서 코드 값을 가져오는 함수
     * @param nullable null 허용 여부
     */
    protected CodeConverter(Class<E> enumClass, Function<E, String> codeGetter, boolean nullable) {
        this.enumClass = enumClass;
        this.codeGetter = codeGetter;
        this.nullable = nullable;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        if (attribute == null) {
            if (!nullable) {
                throw new IllegalArgumentException("Null enum value is not allowed");
            }
            return null;
        }
        return codeGetter.apply(attribute);
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            if (!nullable) {
                throw new IllegalArgumentException("Null database value is not allowed");
            }
            return null;
        }

        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> codeGetter.apply(e).equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown code value: " + dbData + " for enum " + enumClass.getSimpleName()));
    }
}