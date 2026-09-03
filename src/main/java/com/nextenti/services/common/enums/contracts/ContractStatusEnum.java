package com.nextenti.services.common.enums.contracts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.nextenti.services.common.enums.EnumValueHandler;

/**
 * Represents the status of a contract in the SmartRoad platform.
 */
public enum ContractStatusEnum implements EnumValueHandler {

    DRAFT("draft"),
    ACTIVE("active"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    ContractStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ContractStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(ContractStatusEnum.class, value);
    }
}
