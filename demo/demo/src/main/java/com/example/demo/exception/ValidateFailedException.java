package com.example.demo.exception;

public class ValidateFailedException extends RuntimeException {

    public ValidateFailedException() {
        super();
    }

    public ValidateFailedException(String message) {
        super(message);
    }

}
