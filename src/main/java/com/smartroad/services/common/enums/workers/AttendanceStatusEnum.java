package com.smartroad.services.common.enums.workers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents a worker's attendance outcome for a single day on a project.
 */
public enum AttendanceStatusEnum implements EnumValueHandler {

    PRESENT("present"),
    ABSENT("absent"),
    HALF_DAY("half_day"),
    LEAVE("leave");

    private final String value;

    AttendanceStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static AttendanceStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(AttendanceStatusEnum.class, value);
    }
}
