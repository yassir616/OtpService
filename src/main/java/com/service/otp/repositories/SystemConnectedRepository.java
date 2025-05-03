package com.service.otp.repositories;

import com.service.otp.models.SystemConnected;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemConnectedRepository extends JpaRepository<SystemConnected, String> {
    Optional<SystemConnected> findBySystemName(String systemName);
}