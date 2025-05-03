package com.service.otp.services.impl;

import com.service.otp.Exeptions.OtpValidationException;
import com.service.otp.Exeptions.SystemNotFoundException;
import com.service.otp.models.OtpCode;
import com.service.otp.models.OtpRequestLog;
import com.service.otp.models.UserOtp;
import com.service.otp.repositories.OtpCodeRepository;
import com.service.otp.repositories.OtpRequestLogRepository;
import com.service.otp.repositories.UserOtpRepository;
import com.service.otp.requestModels.CreateOtpCodeRequestModel;
import com.service.otp.requestModels.CreateUserRequestModel;
import com.service.otp.services.OtpCodeService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Random;

@Service
@Slf4j
public class OtpCodeServiceImpl implements OtpCodeService {

    private final OtpCodeRepository otpCodeRepository;
    private final UserOtpRepository userOtpRepository;
    private final OtpRequestLogRepository otpRequestLogRepository;
    private final RestTemplate restTemplate;
    private final RateLimiter rateLimiter;

    @Value("${user.service.url}")
    private String userServiceUrl;

    public OtpCodeServiceImpl(OtpCodeRepository otpCodeRepository, UserOtpRepository userOtpRepository,
            OtpRequestLogRepository otpRequestLogRepository, RestTemplate restTemplate,RateLimiter rateLimiter) {
        this.otpCodeRepository = otpCodeRepository;
        this.userOtpRepository = userOtpRepository;
        this.otpRequestLogRepository = otpRequestLogRepository;
        this.restTemplate = restTemplate;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public String genererOtp(CreateOtpCodeRequestModel requestModel) {
        log.info(userServiceUrl);

        rateLimiter.checkRateLimit(requestModel);

        CreateUserRequestModel createUserRequestModel = requestModel.getCreateUserRequestModel();
        UserOtp user = restTemplate.postForObject(
                userServiceUrl + "/findOrCreateUser?systemName=" + requestModel.getSystemName(),
                createUserRequestModel,
                UserOtp.class);

        user = userOtpRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found after creation"));

        // Generation OTP
        log.info("Generating OTP for user: {}", user.getId());
        String otpValue = String.format("%06d", new Random().nextInt(1000000));
        Date expirationDate = new Date(System.currentTimeMillis() + 1 * 60 * 1000);

        OtpCode otpCode = OtpCode.builder()
                .codeValue(otpValue)
                .userOtp(user)
                .expirationDate(expirationDate)
                .build();

        otpCodeRepository.save(otpCode);

        OtpRequestLog otpRequestLog = OtpRequestLog.builder()
                .userOtp(user)
                .requestTime(new Date())
                .build();

        otpRequestLogRepository.save(otpRequestLog);
        log.info("OTP generated: {} for user: {}", otpValue.toString(), requestModel.getCreateUserRequestModel().getUserLogin());
        return otpValue;
    }

    @Override
    public boolean validateOtp(String codeValue, String systemName, String userLogin) {
        boolean systemExists = userOtpRepository.findAll().stream()
                .anyMatch(user -> user.getSystemConnected().getSystemName().equals(systemName));
        if (!systemExists) {
            throw new SystemNotFoundException("Nom de Système Invalide");
        }

        OtpCode otpCode = otpCodeRepository.findValidOtp(codeValue, systemName, userLogin);
        if (otpCode == null) {
            throw new OtpValidationException("Code Invalide");
        }

        if (otpCode.isExpired()) {
            throw new OtpValidationException("Code Expiré");
        }

        return true;
    }
}