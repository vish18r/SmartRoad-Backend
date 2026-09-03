package com.nextenti.services.common.enums.purchase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.nextenti.services.common.enums.EnumValueHandler;

/**
 * Represents the status of a purchase order in the SmartRoad platform.
 */
public enum PurchaseOrderStatusEnum implements EnumValueHandler {

    DRAFT("draft"),
    ORDERED("ordered"),
    PARTIALLY_RECEIVED("partially_received"),
    RECEIVED("received"),
    CANCELLED("cancelled");

    private final String value;

    PurchaseOrderStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PurchaseOrderStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(PurchaseOrderStatusEnum.class, value);
    }
}
