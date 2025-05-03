package com.service.otp.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpRequestLog extends AbstractBaseEntity {


    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserOtp userOtp;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date requestTime;

}