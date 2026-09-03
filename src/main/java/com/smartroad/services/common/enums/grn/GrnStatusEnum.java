package com.smartroad.services.common.enums.grn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the status of a Goods Received Note (GRN) in the SmartRoad platform.
 */
public enum GrnStatusEnum implements EnumValueHandler {

    PENDING("pending"),
    RECEIVED("received"),
    QUALITY_CHECKED("quality_checked"),
    ACCEPTED("accepted"),
    REJECTED("rejected");

    private final String value;

    GrnStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static GrnStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(GrnStatusEnum.class, value);
    }
}
