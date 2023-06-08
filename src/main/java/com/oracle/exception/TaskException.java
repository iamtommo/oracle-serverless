package com.oracle.exception;

public class TaskException extends RuntimeException {

    private final int statusCode;

    public TaskException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
