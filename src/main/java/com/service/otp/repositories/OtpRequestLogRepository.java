package com.service.otp.repositories;

import com.service.otp.models.OtpRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface OtpRequestLogRepository extends JpaRepository<OtpRequestLog, Long> {

    @Query("SELECT COUNT(r) FROM OtpRequestLog r WHERE r.userOtp.login = :userLogin AND r.userOtp.systemConnected.systemName = :systemName AND r.requestTime >= :startTime")
    long countRequestsByLoginAndSystemNameInLast24Hours(@Param("userLogin") String userLogin, 
                                                        @Param("systemName") String systemName, 
                                                        @Param("startTime") Date startTime);

    int deleteByRequestTimeBefore(Date requestTime);
}