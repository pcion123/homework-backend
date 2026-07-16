package com.example.demo.exception;

public class GeneralRuntimeException extends RuntimeException {
    protected int code;

    public GeneralRuntimeException(int code, String message) {
        super(message);

        this.code = code;
    }

    public GeneralRuntimeException(int code, String message, Throwable cause) {
        super(message, cause);

        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
