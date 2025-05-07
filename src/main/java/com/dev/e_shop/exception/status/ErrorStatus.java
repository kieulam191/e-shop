package com.dev.e_shop.exception.status;

public final class ErrorStatus {
    public static final String RES_NOT_FOUND = "Resource not found";
    public static final String PAGE_NOT_FOUND = "Page not found";
    public static final String USERNAME_NOT_FOUND = "Authentication failed";
    public static final String INVALID_REFRESH_TOKEN = "Unauthorized";
    public static final String INVALID_HTTP_STATUS = "Invalid http status code";
    public static final String UNIQUE_CONSTRAINT = "Validation failed for one or more fields.";
    public static final String SQL_CONSTRAINT = "Database constraint violation";
    public static final String METHOD_NOT_SUPPORT = "HTTP method not supported";
    public static final String ALREADY_RES = "Resource already exists";
    public static final String CART_ITEM_NOT_FOUND = "Bad Request";
    public static final String INTERNAL_SERVER = "Internal server error";
    public static final String INVALID_REQUEST_PARAM = "Invalid request parameters";

    private ErrorStatus(){}
}
