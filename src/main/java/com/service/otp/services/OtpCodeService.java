package com.service.otp.services;

import com.service.otp.requestModels.CreateOtpCodeRequestModel;

public interface OtpCodeService {
    String genererOtp(CreateOtpCodeRequestModel requestModel);
    boolean validateOtp(String codeValue, String systemName, String userId);
}