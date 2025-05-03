package com.service.otp.services;

import com.service.otp.models.UserOtp;
import com.service.otp.requestModels.CreateUserRequestModel;

public interface UserOtpService {
    UserOtp save(UserOtp userOtp);
    UserOtp findOrCreateUser(CreateUserRequestModel requestModel,String systemName);
}