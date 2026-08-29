package com.nextenti.services.common.enums.userprofile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.nextenti.services.common.enums.EnumValueHandler;

/**
 * @author Rishikesh
 * @version 1.0
 */
public enum OrganizationUserStatusEnum implements EnumValueHandler {

    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    OrganizationUserStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static OrganizationUserStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(OrganizationUserStatusEnum.class, value);
    }
}
