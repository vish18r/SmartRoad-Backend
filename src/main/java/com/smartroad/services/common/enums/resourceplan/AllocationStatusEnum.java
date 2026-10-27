package com.smartroad.services.common.enums.resourceplan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the status of a single resource allocation within a plan.
 */
public enum AllocationStatusEnum implements EnumValueHandler {

    PLANNED("planned"),
    CONFIRMED("confirmed"),
    CANCELLED("cancelled");

    private final String value;

    AllocationStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AllocationStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(AllocationStatusEnum.class, value);
    }
}
