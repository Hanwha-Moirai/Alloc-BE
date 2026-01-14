package com.moirai.alloc.gantt.common.exception;

import org.springframework.http.HttpStatus;

public class GanttException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    private GanttException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static GanttException badRequest(String message) {
        return new GanttException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }

    public static GanttException forbidden(String message) {
        return new GanttException(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }

    public static GanttException notFound(String message) {
        return new GanttException(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }

    public static GanttException conflict(String message) {
        return new GanttException(HttpStatus.CONFLICT, "CONFLICT", message);
    }

    public static GanttException unauthorized(String message) {
        return new GanttException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
