package com.nextenti.services.common.enums.workers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.nextenti.services.common.enums.EnumValueHandler;

/**
 * Represents the employment and site status of a worker.
 */
public enum WorkerStatusEnum implements EnumValueHandler {

    ACTIVE("active"),
    INACTIVE("inactive"),
    ON_LEAVE("on_leave"),
    SUSPENDED("suspended"),
    TERMINATED("terminated");

    private final String value;

    WorkerStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static WorkerStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(WorkerStatusEnum.class, value);
    }
}
