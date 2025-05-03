package com.service.otp.requestModels;

import lombok.Data;

@Data
public class CreateUserRequestModel {
    String userLogin;
    String phoneNumber;
}
