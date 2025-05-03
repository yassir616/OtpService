package com.service.otp.repositories;

import com.service.otp.models.SystemConnected;
import com.service.otp.models.UserOtp;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOtpRepository extends JpaRepository<UserOtp, String> {
    Optional<UserOtp> findByLoginAndSystemConnected(String login, SystemConnected system);
}