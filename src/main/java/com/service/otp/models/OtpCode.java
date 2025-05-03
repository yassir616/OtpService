package com.service.otp.models;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class OtpCode extends AbstractBaseEntity {

    private static final long serialVersionUID = 1L;

    private String codeValue;

    @ManyToOne
    private UserOtp userOtp;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    public boolean isExpired() {
        return new Date().after(this.expirationDate);
    }
}
