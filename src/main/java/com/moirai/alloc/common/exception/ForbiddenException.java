package com.moirai.alloc.common.exception;

public class ForbiddenException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.FORBIDDEN;

    public ForbiddenException(String message) {
        super(message);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
