package com.smartroad.services.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserType {
    INDIVIDUAL("INDIVIDUAL"),
    CORPORATE("CORPORATE"),
    GOVERNMENT("GOVERNMENT");

    private final String value;

    UserType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
