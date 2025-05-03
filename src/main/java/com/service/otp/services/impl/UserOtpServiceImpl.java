package com.service.otp.services.impl;

import com.service.otp.models.SystemConnected;
import com.service.otp.models.UserOtp;
import com.service.otp.repositories.SystemConnectedRepository;
import com.service.otp.repositories.UserOtpRepository;
import com.service.otp.requestModels.CreateUserRequestModel;
import com.service.otp.services.UserOtpService;
import org.springframework.stereotype.Service;

@Service
public class UserOtpServiceImpl implements UserOtpService {

    private final UserOtpRepository userOtpRepository;
    private final SystemConnectedRepository systemConnectedRepository;

    public UserOtpServiceImpl(UserOtpRepository userOtpRepository, SystemConnectedRepository systemConnectedRepository) {
        this.userOtpRepository = userOtpRepository;
        this.systemConnectedRepository = systemConnectedRepository;
    }

    @Override
    public UserOtp save(UserOtp userOtp) {
        return userOtpRepository.save(userOtp);
    }

    @Override
    public UserOtp findOrCreateUser(CreateUserRequestModel requestModel, String systemName) {
        SystemConnected system = systemConnectedRepository.findBySystemName(systemName)
                .orElseThrow(() -> new RuntimeException("System not found"));

        return userOtpRepository.findByLoginAndSystemConnected(requestModel.getUserLogin(), system)
                .orElseGet(() -> {
                    UserOtp newUser = new UserOtp();
                    newUser.setPhoneNumber(requestModel.getPhoneNumber());
                    newUser.setLogin(requestModel.getUserLogin());
                    newUser.setSystemConnected(system);

                    return userOtpRepository.save(newUser);
                });
    }
}