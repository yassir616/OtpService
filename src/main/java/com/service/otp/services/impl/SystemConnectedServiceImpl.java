package com.service.otp.services.impl;

import com.service.otp.models.SystemConnected;
import com.service.otp.repositories.SystemConnectedRepository;
import com.service.otp.services.SystemConnectedService;
import org.springframework.stereotype.Service;


@Service
public class SystemConnectedServiceImpl implements SystemConnectedService {

    private final SystemConnectedRepository repository;

    public SystemConnectedServiceImpl(SystemConnectedRepository repository) {
        this.repository = repository;
    }

    @Override
    public SystemConnected save(SystemConnected systemConnected) {
        return repository.save(systemConnected);
    }

}