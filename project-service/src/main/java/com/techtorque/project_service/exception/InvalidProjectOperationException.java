package com.techtorque.project_service.exception;

public class InvalidProjectOperationException extends RuntimeException {
    public InvalidProjectOperationException(String message) {
        super(message);
    }

    public InvalidProjectOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
