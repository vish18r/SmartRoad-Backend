package com.smartroad.services.common.enums.resourceplan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the kind of resource an allocation refers to.
 */
public enum ResourceTypeEnum implements EnumValueHandler {

    WORKER("worker"),
    MACHINE("machine"),
    MATERIAL("material"),
    VEHICLE("vehicle");

    private final String value;

    ResourceTypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ResourceTypeEnum fromValue(String value) {
        return EnumValueHandler.fromValue(ResourceTypeEnum.class, value);
    }
}
