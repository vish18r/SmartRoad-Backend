package com.nextenti.services.common.enums.stock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.nextenti.services.common.enums.EnumValueHandler;

/**
 * Represents the status of a stock transfer in the SmartRoad platform.
 */
public enum StockTransferStatusEnum implements EnumValueHandler {

    REQUESTED("requested"),
    APPROVED("approved"),
    IN_TRANSIT("in_transit"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    StockTransferStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static StockTransferStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(StockTransferStatusEnum.class, value);
    }
}
