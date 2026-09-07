package com.smartroad.services.common.enums.businessprofile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the role of a contact associated with a business profile.
 */
public enum ContactRoleEnum implements EnumValueHandler {

    MAIN_FOUNDER("main_founder"),
    ADDITIONAL_CONTACT("additional_contact");

    private final String value;

    ContactRoleEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ContactRoleEnum fromValue(String value) {
        return EnumValueHandler.fromValue(ContactRoleEnum.class, value);
    }
}
