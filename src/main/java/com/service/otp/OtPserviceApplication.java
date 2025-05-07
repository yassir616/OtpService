package com.service.otp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OtPserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtPserviceApplication.class, args);
    }

}
