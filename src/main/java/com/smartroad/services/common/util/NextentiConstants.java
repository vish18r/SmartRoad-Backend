package com.smartroad.services.common.util;

/**
 * Shared constant definitions used across the application.
 * Holds the correlation-ID header name and the {@code x-*} filter headers the web client
 * uses to pass list filtering, search, and pagination options.
 */
public final class NextentiConstants {

    public static final String CORRELATION_ID = "correlation_id";

    public static final String HEADER_PROJECT_ID = "x-project-id";

    public static final String HEADER_SEARCH = "x-search";

    public static final String HEADER_PAGE = "x-page";

    public static final String HEADER_LIMIT = "x-limit";

    public static final String HEADER_MONTH = "x-month";

    public static final int DEFAULT_PAGE = 1;

    public static final int DEFAULT_LIMIT = 20;

    /**
     * Prevents instantiation of this constant holder.
     */
    private NextentiConstants() {
    }
}
