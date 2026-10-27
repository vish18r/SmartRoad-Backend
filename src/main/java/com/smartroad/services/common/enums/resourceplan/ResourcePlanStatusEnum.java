package com.smartroad.services.common.enums.resourceplan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the lifecycle status of a resource plan.
 */
public enum ResourcePlanStatusEnum implements EnumValueHandler {

    DRAFT("draft"),
    PLANNED("planned"),
    CONFIRMED("confirmed"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    ResourcePlanStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ResourcePlanStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(ResourcePlanStatusEnum.class, value);
    }
}
