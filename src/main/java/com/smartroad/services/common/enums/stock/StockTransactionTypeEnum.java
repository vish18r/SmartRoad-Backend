package com.smartroad.services.common.enums.stock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the kind of movement recorded by a single stock ledger entry.
 */
public enum StockTransactionTypeEnum implements EnumValueHandler {

    RECEIVED("received"),
    ISSUED("issued"),
    TRANSFER("transfer"),
    ADJUSTMENT("adjustment"),
    CONSUMPTION("consumption");

    private final String value;

    StockTransactionTypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static StockTransactionTypeEnum fromValue(String value) {
        return EnumValueHandler.fromValue(StockTransactionTypeEnum.class, value);
    }
}
