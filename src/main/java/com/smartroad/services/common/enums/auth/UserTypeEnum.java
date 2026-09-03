package com.smartroad.services.common.enums.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * @author Rishikesh
 * @version 1.0
 */
public enum UserTypeEnum implements EnumValueHandler {

    ORGANIZATION("organization"),
    CANDIDATE("candidate"),
    NEXTENTI("nextenti");

    private final String value;

    UserTypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserTypeEnum fromValue(String value) {
        return EnumValueHandler.fromValue(UserTypeEnum.class, value);
    }
}
