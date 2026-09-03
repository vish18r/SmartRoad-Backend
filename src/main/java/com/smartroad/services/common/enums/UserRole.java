package com.smartroad.services.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    ADMIN("ADMIN"),
    MANAGER("MANAGER"),
    SUPERVISOR("SUPERVISOR"),
    WORKER("WORKER"),
    USER("USER");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
