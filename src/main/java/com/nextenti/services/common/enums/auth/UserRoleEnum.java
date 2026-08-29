package com.nextenti.services.common.enums.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.nextenti.services.common.enums.EnumValueHandler;

/**
 * @author Rishikesh
 * @version 1.0
 */
public enum UserRoleEnum implements EnumValueHandler {

    ADMIN("admin"),
    USER("user"),
    ROOT_USER("root_user");

    private final String value;

    UserRoleEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserRoleEnum fromValue(String value) {
        return EnumValueHandler.fromValue(UserRoleEnum.class, value);
    }
}
