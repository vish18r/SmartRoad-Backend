package com.nextenti.services.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OtpFlow {
    SIGNUP_VERIFICATION("SIGNUP_VERIFICATION"),
    EMAIL_VERIFICATION("EMAIL_VERIFICATION"),
    PASSWORD_RESET("PASSWORD_RESET"),
    PHONE_VERIFICATION("PHONE_VERIFICATION");

    private final String value;

    OtpFlow(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
