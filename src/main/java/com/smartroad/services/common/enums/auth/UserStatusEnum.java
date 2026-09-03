package com.smartroad.services.common.enums.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * @author Rishikesh
 * @version 1.0
 */
public enum UserStatusEnum implements EnumValueHandler {

    ACTIVE("active"),
    INACTIVE("inactive"),
    PENDING("pending"),
    DELETED("deleted"),
    BLOCKED("blocked");

    private final String value;

    UserStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(UserStatusEnum.class, value);
    }
}
