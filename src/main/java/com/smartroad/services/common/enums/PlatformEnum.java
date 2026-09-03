package com.smartroad.services.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Shayesta
 * @version 1.0
 */
public enum PlatformEnum implements EnumValueHandler {

    IOS("ios"),
    ANDROID("android"),
    WEB("web");

    private final String value;

    PlatformEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PlatformEnum fromValue(String value) {
        return EnumValueHandler.fromValue(PlatformEnum.class, value);
    }
}
