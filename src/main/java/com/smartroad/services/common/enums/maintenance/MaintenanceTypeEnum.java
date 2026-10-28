package com.smartroad.services.common.enums.maintenance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the kind of maintenance work: planned preventive servicing or corrective repair after a fault/breakdown.
 */
public enum MaintenanceTypeEnum implements EnumValueHandler {

    PREVENTIVE("preventive"),
    CORRECTIVE("corrective");

    private final String value;

    MaintenanceTypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MaintenanceTypeEnum fromValue(String value) {
        return EnumValueHandler.fromValue(MaintenanceTypeEnum.class, value);
    }
}
