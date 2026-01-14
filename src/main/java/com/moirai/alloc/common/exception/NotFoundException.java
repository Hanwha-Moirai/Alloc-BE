package com.moirai.alloc.common.exception;

public class NotFoundException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.NOT_FOUND;

    public NotFoundException(String message) {
        super(message);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
