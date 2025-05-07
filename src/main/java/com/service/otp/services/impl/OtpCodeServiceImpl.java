package com.service.otp.services.impl;

import com.service.otp.Exeptions.OtpValidationException;
import com.service.otp.Exeptions.SystemNotFoundException;
import com.service.otp.models.OtpCode;
import com.service.otp.models.OtpRequestLog;
import com.service.otp.models.SystemConnected;
import com.service.otp.models.UserOtp;
import com.service.otp.repositories.OtpCodeRepository;
import com.service.otp.repositories.OtpRequestLogRepository;
import com.service.otp.repositories.SystemConnectedRepository;
import com.service.otp.repositories.UserOtpRepository;
import com.service.otp.requestModels.CreateOtpCodeRequestModel;
import com.service.otp.requestModels.CreateUserRequestModel;
import com.service.otp.services.OtpCodeService;
import com.service.otp.services.UserOtpService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.text.SimpleDateFormat;

@Service
@Slf4j
public class OtpCodeServiceImpl implements OtpCodeService {

        private final OtpCodeRepository otpCodeRepository;
        private final UserOtpRepository userOtpRepository;
        private final OtpRequestLogRepository otpRequestLogRepository;
        private final RestTemplate restTemplate;
        private final RateLimiter rateLimiter;
        private final SmsService smsService;
        private UserOtpService userOtpService;
        private final SystemConnectedRepository systemConnectedRepository;

        @Value("${user.service.url}")
        private String userServiceUrl;

        public OtpCodeServiceImpl(OtpCodeRepository otpCodeRepository, UserOtpRepository userOtpRepository,
                        OtpRequestLogRepository otpRequestLogRepository,
                        RestTemplate restTemplate, RateLimiter rateLimiter, SmsService smsService,
                        SystemConnectedRepository systemConnectedRepository, UserOtpService userOtpService) {
                this.otpCodeRepository = otpCodeRepository;
                this.userOtpRepository = userOtpRepository;
                this.otpRequestLogRepository = otpRequestLogRepository;
                this.restTemplate = restTemplate;
                this.rateLimiter = rateLimiter;
                this.smsService = smsService;
                this.systemConnectedRepository = systemConnectedRepository;
                this.userOtpService = userOtpService;
        }

        @Override
        @Transactional
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

                if (user.getFailedAttempt() != null && user.getFailedAttempt() >= 4 && user.getBlockDate() != null) {
                        if (user.getBlockDate().after(new Date())) {
                                throw new OtpValidationException("Trop de tentatives échouées, Utilisateur bloqué.");
                        } else {
                                userOtpService.resetFailedAttemptsUser(user.getId());
                        }
                }

                log.info("Generating OTP for user: {}", user.getId());
                String otpValue = String.format("%06d", new Random().nextInt(1000000));
                Date expirationDate = new Date(System.currentTimeMillis() + 1 * 60 * 1000);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String formattedExpirationDate = dateFormat.format(expirationDate);

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

                String phoneNumber = requestModel.getCreateUserRequestModel().getPhoneNumber();
                String message = otpValue + " est le code pour valider votre authentification sur systeme : "
                                + requestModel.getSystemName()
                                + " valide jusqu'a : "
                                + formattedExpirationDate;
                log.info(message);
                smsService.sendSms(phoneNumber, message);
                log.info("OTP generated: {} for user: {}", otpValue.toString(),
                                requestModel.getCreateUserRequestModel().getUserLogin());
                userOtpService.resetFailedAttemptsUser(user.getId());

                return otpValue;
        }

        @Override
        @Transactional
        public boolean validateOtp(String codeValue, String systemName, String userLogin) {
                Optional<SystemConnected> systemExists = systemConnectedRepository.findBySystemName(systemName);
                if (!systemExists.isPresent()) {
                        throw new SystemNotFoundException("Demande Invalide");
                }

                // Track failed attempts
                UserOtp user = userOtpRepository.findAll().stream()
                                .filter(u -> u.getLogin().equals(userLogin)
                                                && u.getSystemConnected().getSystemName().equals(systemName))
                                .findFirst()
                                .orElseThrow(() -> new SystemNotFoundException("Utilisateur non trouvé"));

                // Check if the user is blocked and unblock if the latest OTP has expired
                if (user.getFailedAttempt() != null && user.getFailedAttempt() >= 4 && user.getBlockDate() != null) {
                        if (user.getBlockDate().after(new Date())) {
                                throw new OtpValidationException("Trop de tentatives échouées, Utilisateur bloqué.");
                        } else {
                                userOtpService.resetFailedAttemptsUser(user.getId());
                        }
                }

                OtpCode otpCode = otpCodeRepository.findValidOtp(codeValue, systemName, userLogin);
                if (otpCode == null) {
                        userOtpService.incrementFailedAttemptsAndBlockUser(user.getId());
                        throw new OtpValidationException("Code Invalide");
                }

                if (otpCode.isExpired()) {
                        throw new OtpValidationException("Code Expiré");
                }

                // Reset failed attempts on successful validation
                if (user.getFailedAttempt() != null) {
                        userOtpService.resetFailedAttemptsUser(user.getId());
                }

                return true;
        }
}