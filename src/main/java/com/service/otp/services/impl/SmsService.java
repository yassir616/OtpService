package com.service.otp.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.service.otp.Exeptions.SmsServiceException;
import com.service.otp.requestModels.CreateSmsRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmsService {

    @Value("${sms.service.url}")
    private String smsProviderUrl;

    @Value("${sms.service.from}")
    private String smsFrom;

    @Value("${sms.service.domain-id}")
    private int smsDomainId;

    @Value("${sms.service.reason-id}")
    private int smsReasonId;

    @Value("${sms.service.complement}")
    private String smsComplement;

    @Value("${sms.service.complement-id}")
    private int smsComplementId;

    private final WebClient webClient;

    @Autowired
    public SmsService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String sendSms(String phoneNumber, String message) {
        CreateSmsRequest smsRequest = new CreateSmsRequest(
                phoneNumber, 
                message, 
                smsFrom, 
                smsDomainId, 
                smsReasonId, 
                smsComplement, 
                smsComplementId
        );

        log.info(smsRequest.toString());

        try {
            return webClient.post()
                    .uri(smsProviderUrl)
                    .bodyValue(smsRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.info("Échec de l'envoi du SMS a: {}", phoneNumber);
            throw new SmsServiceException("Échec de l'envoi du SMS : " + e.getMessage(), e);
        }
    }
}