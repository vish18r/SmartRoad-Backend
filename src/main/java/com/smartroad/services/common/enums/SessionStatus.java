package com.smartroad.services.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SessionStatus {
    ACTIVE("ACTIVE"),
    REVOKED("REVOKED"),
    EXPIRED("EXPIRED");

    private final String value;

    SessionStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
