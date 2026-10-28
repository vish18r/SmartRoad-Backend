package com.smartroad.services.common.enums.maintenance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the event a maintenance reminder/notification is raised for.
 */
public enum MaintenanceReminderTypeEnum implements EnumValueHandler {

    CREATED("created"),
    DUE("due"),
    OVERDUE("overdue"),
    COMPLETED("completed");

    private final String value;

    MaintenanceReminderTypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MaintenanceReminderTypeEnum fromValue(String value) {
        return EnumValueHandler.fromValue(MaintenanceReminderTypeEnum.class, value);
    }
}
