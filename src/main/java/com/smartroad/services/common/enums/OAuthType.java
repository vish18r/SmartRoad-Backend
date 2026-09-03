package com.smartroad.services.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OAuthType {
    GOOGLE("GOOGLE"),
    APPLE("APPLE"),
    FACEBOOK("FACEBOOK");

    private final String value;

    OAuthType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
