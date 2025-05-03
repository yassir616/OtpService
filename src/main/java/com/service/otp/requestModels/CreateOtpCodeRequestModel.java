package com.service.otp.requestModels;

import lombok.Data;

@Data
public class CreateOtpCodeRequestModel {
    private String systemName;
    private CreateUserRequestModel createUserRequestModel;
}
