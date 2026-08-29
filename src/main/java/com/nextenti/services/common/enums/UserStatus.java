package com.nextenti.services.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserStatus {
    ACTIVE("ACTIVE"),
    PENDING("PENDING"),
    BLOCKED("BLOCKED"),
    DISABLED("DISABLED"),
    DELETED("DELETED");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
