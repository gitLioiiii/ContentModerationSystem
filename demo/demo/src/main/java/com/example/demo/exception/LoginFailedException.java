package com.example.demo.exception;

public class LoginFailedException extends RuntimeException {

    public LoginFailedException() {
        super();
    }

    public LoginFailedException(String message) {
        super(message);
    }
    
}
