package com.nextenti.services.common.enums.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.nextenti.services.common.enums.EnumValueHandler;

/**
 * @author Shayesta
 * @version 1.0
 */
public enum UserCreationMethodEnum implements EnumValueHandler {

    DIRECT("direct"),
    GOOGLE("google"),
    APPLE("apple");

    private final String value;

    UserCreationMethodEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserCreationMethodEnum fromValue(String value) {
        return EnumValueHandler.fromValue(UserCreationMethodEnum.class, value);
    }
}
