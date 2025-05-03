package com.service.otp.Exeptions;

public class OtpValidationException extends RuntimeException {
    public OtpValidationException(String message) {
        super(message);
    }
}