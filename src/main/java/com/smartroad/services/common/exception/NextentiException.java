package com.smartroad.services.common.exception;

public class NextentiException extends Exception {

    private final ApplicationLayer applicationLayer;
    private final ErrorCodeMapping errorCode;
    private final String messageKey;
    private final transient Object[] arguments;

    public NextentiException(ApplicationLayer applicationLayer, ErrorCodeMapping errorCode, String messageKey) {
        this(applicationLayer, errorCode, messageKey, (Object[]) null);
    }

    public NextentiException(ApplicationLayer applicationLayer, ErrorCodeMapping errorCode, String messageKey, Object... arguments) {
        super(messageKey);
        this.applicationLayer = applicationLayer;
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.arguments = arguments;
    }

    public ApplicationLayer getApplicationLayer() {
        return applicationLayer;
    }

    public ErrorCodeMapping getErrorCode() {
        return errorCode;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArguments() {
        return arguments;
    }
}
