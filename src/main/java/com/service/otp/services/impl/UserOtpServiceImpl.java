package com.service.otp.services.impl;

import com.service.otp.Exeptions.SystemNotFoundException;
import com.service.otp.models.SystemConnected;
import com.service.otp.models.UserOtp;
import com.service.otp.repositories.SystemConnectedRepository;
import com.service.otp.repositories.UserOtpRepository;
import com.service.otp.requestModels.CreateUserRequestModel;
import com.service.otp.services.UserOtpService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import java.util.Date;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserOtpServiceImpl implements UserOtpService {

    private final UserOtpRepository userOtpRepository;
    private final SystemConnectedRepository systemConnectedRepository;
    private final Validator validator;


    public UserOtpServiceImpl(UserOtpRepository userOtpRepository, SystemConnectedRepository systemConnectedRepository,
            Validator validator) {
        this.userOtpRepository = userOtpRepository;
        this.systemConnectedRepository = systemConnectedRepository;
        this.validator = validator;
    }

    @Override
    public UserOtp save(UserOtp userOtp) {
        return userOtpRepository.save(userOtp);
    }

    @Override
    public UserOtp findOrCreateUser(CreateUserRequestModel requestModel, String systemName) {
        SystemConnected system = systemConnectedRepository.findBySystemName(systemName)
                .orElseThrow(() -> new SystemNotFoundException("Système non trouvé"));

        return userOtpRepository.findByLoginAndSystemConnected(requestModel.getUserLogin(), system)
                .orElseGet(() -> {
                    UserOtp newUser = new UserOtp();
                    newUser.setPhoneNumber(requestModel.getPhoneNumber());
                    newUser.setLogin(requestModel.getUserLogin());
                    newUser.setSystemConnected(system);
                    validateUserOtp(newUser);
                    return userOtpRepository.save(newUser);
                });
    }

    private void validateUserOtp(UserOtp userOtp) {
        Set<ConstraintViolation<UserOtp>> violations = validator.validate(userOtp);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttemptsUser(String id) {
        UserOtp userOtp = userOtpRepository.findById(id)
                .orElseThrow(() -> new SystemNotFoundException("User not found"));
        userOtp.setFailedAttempt(null);
        userOtp.setBlockDate(null);
        userOtpRepository.save(userOtp);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementFailedAttemptsAndBlockUser(String userId) {
        UserOtp userOtp = userOtpRepository.findById(userId)
                .orElseThrow(() -> new SystemNotFoundException("User not found"));
        userOtp.setFailedAttempt(userOtp.getFailedAttempt() == null ? 1 : userOtp.getFailedAttempt() + 1);
        if (userOtp.getFailedAttempt() >= 4) {
            Date blockDate = new Date();
            blockDate.setTime(blockDate.getTime() + 60 * 1000);
            userOtp.setBlockDate(blockDate);
        }
        userOtpRepository.save(userOtp);
    }

}