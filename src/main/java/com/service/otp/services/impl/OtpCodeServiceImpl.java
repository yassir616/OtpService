package com.service.otp.services.impl;

import com.service.otp.models.OtpCode;
import com.service.otp.models.OtpRequestLog;
import com.service.otp.models.UserOtp;
import com.service.otp.repositories.OtpCodeRepository;
import com.service.otp.repositories.OtpRequestLogRepository;
import com.service.otp.repositories.UserOtpRepository;
import com.service.otp.requestModels.CreateOtpCodeRequestModel;
import com.service.otp.requestModels.CreateUserRequestModel;
import com.service.otp.services.OtpCodeService;
import com.service.otp.utils.Constants;

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

    @Value("${user.service.url}")
    private String userServiceUrl;

    public OtpCodeServiceImpl(OtpCodeRepository otpCodeRepository, UserOtpRepository userOtpRepository,
            OtpRequestLogRepository otpRequestLogRepository, RestTemplate restTemplate) {
        this.otpCodeRepository = otpCodeRepository;
        this.userOtpRepository = userOtpRepository;
        this.otpRequestLogRepository = otpRequestLogRepository;
        this.restTemplate = restTemplate;

    }

    @Override
    public String genererOtp(CreateOtpCodeRequestModel requestModel) {
        log.info(userServiceUrl);

        long requestCount = otpRequestLogRepository.countRequestsByLoginAndSystemNameInLast24Hours(
                requestModel.getCreateUserRequestModel().getUserLogin(),
                requestModel.getSystemName(),
                new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));

        if (requestCount >= Constants.MAX_REQUESTS_PER_DAY) {
            throw new RuntimeException("Rate limit exceeded. Please try again later.");
        }

        CreateUserRequestModel createUserRequestModel = requestModel.getCreateUserRequestModel();
        String systemName = requestModel.getSystemName();

        // Call the findOrCreateUser endpoint with a request body
        UserOtp user = restTemplate.postForObject(
                userServiceUrl + "/findOrCreateUser?systemName=" + systemName,
                createUserRequestModel,
                UserOtp.class);

        // Reattach the UserOtp entity to the current Hibernate session
        user = userOtpRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found after creation"));

        // Generate OTP
        String otpValue = String.format("%06d", new Random().nextInt(1000000));
        Date expirationDate = new Date(System.currentTimeMillis() + 1 * 60 * 1000);

        OtpCode otpCode = OtpCode.builder()
                .codeValue(otpValue)
                .userOtp(user)
                .expirationDate(expirationDate)
                .build();

        otpCodeRepository.save(otpCode);

        // Log the OTP request
        OtpRequestLog log = OtpRequestLog.builder()
                .userOtp(user) // Ensure userOtp is properly set
                .requestTime(new Date())
                .build();

        otpRequestLogRepository.save(log);

        return otpValue;
    }

    @Override
    public boolean validateOtp(String codeValue, String systemName, String userLogin) {
        boolean systemExists = userOtpRepository.findAll().stream()
                .anyMatch(user -> user.getSystemConnected().getSystemName().equals(systemName));
        if (!systemExists) {
            throw new RuntimeException("Nom de Système Invalide");
        }

        OtpCode otpCode = otpCodeRepository.findValidOtp(codeValue, systemName, userLogin);
        if (otpCode == null) {
            throw new RuntimeException("Invalid Code");
        }

        if (otpCode.isExpired()) {
            throw new RuntimeException("Code Expiré");
        }

        return true;
    }
}