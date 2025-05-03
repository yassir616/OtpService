package com.service.otp.Exeptions;

public class SystemNotFoundException extends RuntimeException {
    public SystemNotFoundException(String message) {
        super(message);
    }
}

