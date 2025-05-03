package com.service.otp.requestModels;

import lombok.Data;

@Data
public class ValidateOtpRequestModel {
    String userLogin;
    String systemName;
    String codeValue;
}
