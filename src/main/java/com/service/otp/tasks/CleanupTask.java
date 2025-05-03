package com.service.otp.tasks;

import com.service.otp.repositories.OtpCodeRepository;
import com.service.otp.repositories.OtpRequestLogRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CleanupTask {

    private final OtpCodeRepository otpCodeRepository;
    private final OtpRequestLogRepository otpRequestLogRepository;

    public CleanupTask(OtpCodeRepository otpCodeRepository, OtpRequestLogRepository otpRequestLogRepository) {
        this.otpCodeRepository = otpCodeRepository;
        this.otpRequestLogRepository = otpRequestLogRepository;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanExpiredData() {
        Date cutoffDate = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(48));

        log.info("Début du nettoyage des codes OTP expirés et des journaux de requêtes plus anciens que 48 heures.");

        int deletedOtps = otpCodeRepository.deleteByExpirationDateBefore(cutoffDate);
        log.info("Supprimé {} OTP expirés.", deletedOtps);

        int deletedLogs = otpRequestLogRepository.deleteByRequestTimeBefore(cutoffDate);
        log.info("Supprimé {} journaux de requêtes expirés.", deletedLogs);

        log.info("Tâche de nettoyage terminée.");
    }
}
