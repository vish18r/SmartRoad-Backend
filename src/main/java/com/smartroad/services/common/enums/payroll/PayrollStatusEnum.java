package com.smartroad.services.common.enums.payroll;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the lifecycle status of a payroll record, from first calculation through payment.
 */
public enum PayrollStatusEnum implements EnumValueHandler {

    DRAFT("draft"),
    PROCESSED("processed"),
    APPROVED("approved"),
    PAID("paid"),
    CANCELLED("cancelled");

    private final String value;

    PayrollStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PayrollStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(PayrollStatusEnum.class, value);
    }
}
