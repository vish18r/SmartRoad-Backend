package com.smartroad.services.common.enums.maintenance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the lifecycle status of a maintenance record. Only SCHEDULED, IN_PROGRESS, COMPLETED and CANCELLED are persisted; DUE and OVERDUE are derived on read from the schedule and the current time.
 */
public enum MaintenanceStatusEnum implements EnumValueHandler {

    SCHEDULED("scheduled"),
    DUE("due"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    OVERDUE("overdue"),
    CANCELLED("cancelled");

    private final String value;

    MaintenanceStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MaintenanceStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(MaintenanceStatusEnum.class, value);
    }
}
