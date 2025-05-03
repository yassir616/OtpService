package com.service.otp.repositories;

import com.service.otp.models.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, String> {
    OtpCode findByCodeValue(String codeValue);

    @Query("SELECT o FROM OtpCode o WHERE o.codeValue = :codeValue AND o.userOtp.systemConnected.systemName = :systemName AND o.userOtp.login = :userLogin ")
    OtpCode findValidOtp(@Param("codeValue") String codeValue, 
                     @Param("systemName") String systemName, 
                     @Param("userLogin") String userLogin);

    int deleteByExpirationDateBefore(Date expirationDate);
}