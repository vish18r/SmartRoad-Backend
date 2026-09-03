package com.smartroad.services.common.enums.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * @author Rishikesh
 * @version 1.0
 */
public enum UserAuditLogStatusEnum implements EnumValueHandler {

    ACTIVE("active"),
    BLOCKED("blocked"),
    DELETED("deleted"),
    PENDING("pending"),
    RESTORE("restore"),
    UNBLOCK("unblock");

    private final String value;

    UserAuditLogStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserAuditLogStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(UserAuditLogStatusEnum.class, value);
    }
}
