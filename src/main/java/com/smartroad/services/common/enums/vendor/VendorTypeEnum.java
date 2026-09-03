package com.smartroad.services.common.enums.vendor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the type of vendor/supplier in the SmartRoad platform.
 */
public enum VendorTypeEnum implements EnumValueHandler {

    MATERIAL("material"),
    LABOUR("labour"),
    EQUIPMENT("equipment"),
    SERVICES("services"),
    OTHER("other");

    private final String value;

    VendorTypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static VendorTypeEnum fromValue(String value) {
        return EnumValueHandler.fromValue(VendorTypeEnum.class, value);
    }
}
