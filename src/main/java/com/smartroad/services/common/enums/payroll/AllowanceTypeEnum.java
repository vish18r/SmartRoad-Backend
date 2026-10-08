package com.smartroad.services.common.enums.payroll;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the category of an itemized payroll allowance.
 */
public enum AllowanceTypeEnum implements EnumValueHandler {

    TRAVEL("travel"),
    FOOD("food"),
    ACCOMMODATION("accommodation"),
    SITE("site"),
    PERFORMANCE("performance"),
    OTHER("other");

    private final String value;

    AllowanceTypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AllowanceTypeEnum fromValue(String value) {
        return EnumValueHandler.fromValue(AllowanceTypeEnum.class, value);
    }
}
