package com.smartroad.services.common.enums.payroll;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the category of an itemized payroll deduction.
 */
public enum DeductionTypeEnum implements EnumValueHandler {

    SALARY_ADVANCE("salary_advance"),
    LOAN_DEDUCTION("loan_deduction"),
    LATE_DEDUCTION("late_deduction"),
    ABSENCE_DEDUCTION("absence_deduction"),
    OTHER_DEDUCTION("other_deduction");

    private final String value;

    DeductionTypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DeductionTypeEnum fromValue(String value) {
        return EnumValueHandler.fromValue(DeductionTypeEnum.class, value);
    }
}
