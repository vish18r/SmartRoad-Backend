package com.nextenti.services.core.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nextenti.services.common.enums.UserRole;
import com.nextenti.services.common.enums.UserStatus;
import com.nextenti.services.common.enums.UserType;
import lombok.Builder;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String countryCode;
    private UserRole role;
    private UserType userType;
    private UserStatus status;
    private Boolean emailVerified;

    public UserResponse() {
    }

    @Builder
    public UserResponse(UUID id, String firstName, String lastName, String email, String phoneNumber, String countryCode, UserRole role, UserType userType, UserStatus status, Boolean emailVerified) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.countryCode = countryCode;
        this.role = role;
        this.userType = userType;
        this.status = status;
        this.emailVerified = emailVerified;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
