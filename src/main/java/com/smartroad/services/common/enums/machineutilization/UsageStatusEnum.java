package com.smartroad.services.common.enums.machineutilization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the recorded status of a machine during a usage session, distinguishing confirmed
 * running/idle time from scheduled-but-unused time, maintenance downtime, and unknown/unrecorded
 * time so that unrecorded hours are never silently treated as idle.
 */
public enum UsageStatusEnum implements EnumValueHandler {

    RUNNING("running"),
    IDLE("idle"),
    SCHEDULED_UNUSED("scheduled_unused"),
    MAINTENANCE_UNAVAILABLE("maintenance_unavailable"),
    UNKNOWN("unknown");

    private final String value;

    UsageStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UsageStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(UsageStatusEnum.class, value);
    }
}
