package com.nextenti.services.common.enums.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.nextenti.services.common.enums.EnumValueHandler;

/**
 * @author Shayesta
 * @version 1.0
 */
public enum UserRegistrationSourceEnum implements EnumValueHandler {

    DIRECT_SIGNUP("direct_signup"),
    SOCIAL_GOOGLE("social_google"),
    SOCIAL_APPLE("social_apple");

    private final String value;

    UserRegistrationSourceEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserRegistrationSourceEnum fromValue(String value) {
        return EnumValueHandler.fromValue(UserRegistrationSourceEnum.class, value);
    }
}
