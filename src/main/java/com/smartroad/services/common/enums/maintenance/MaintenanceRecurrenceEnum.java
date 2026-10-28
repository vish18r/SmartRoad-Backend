package com.smartroad.services.common.enums.maintenance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents how often a preventive maintenance task repeats. Only one future occurrence is ever generated at a time (see docs/MAINTENANCE_MANAGEMENT.md).
 */
public enum MaintenanceRecurrenceEnum implements EnumValueHandler {

    ONE_TIME("one_time"),
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly");

    private final String value;

    MaintenanceRecurrenceEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MaintenanceRecurrenceEnum fromValue(String value) {
        return EnumValueHandler.fromValue(MaintenanceRecurrenceEnum.class, value);
    }
}
