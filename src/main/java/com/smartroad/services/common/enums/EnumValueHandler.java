package com.smartroad.services.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public interface EnumValueHandler {

    @JsonValue
    String getValue();

    static <E extends Enum<E> & EnumValueHandler> E fromValue(Class<E> enumClass, String value) {
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.getValue().equalsIgnoreCase(value) || constant.name().equalsIgnoreCase(value)) {
                return constant;
            }
        }
        throw new IllegalArgumentException("Unknown value '" + value + "' for enum " + enumClass.getSimpleName());
    }
}
