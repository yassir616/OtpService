package com.service.otp.services.impl;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.service.otp.Exeptions.RateLimitExceededException;
import com.service.otp.repositories.OtpRequestLogRepository;
import com.service.otp.requestModels.CreateOtpCodeRequestModel;
import com.service.otp.utils.Constants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RateLimiter {

    private final OtpRequestLogRepository otpRequestLogRepository;

    public RateLimiter(OtpRequestLogRepository otpRequestLogRepository) {
        this.otpRequestLogRepository = otpRequestLogRepository;
    }

    public void checkRateLimit(CreateOtpCodeRequestModel requestModel) {
        long requestCount = otpRequestLogRepository.countRequestsByLoginAndSystemNameInLast24Hours(
                requestModel.getCreateUserRequestModel().getUserLogin(),
                requestModel.getSystemName(),  
                new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)));
        if (requestCount >= Constants.MAX_REQUESTS_PER_DAY) {
            log.info("Vérification de la limite de taux réussie pour l'utilisateur : {}", requestModel.getCreateUserRequestModel().getUserLogin());
            throw new RateLimitExceededException("Limite de taux dépassée. Veuillez réessayer plus tard.");
        }
    }
}