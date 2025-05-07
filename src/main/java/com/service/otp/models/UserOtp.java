package com.service.otp.models;


import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOtp extends AbstractBaseEntity {

    @NotNull(message = "Le login ne peut pas être nul")
    private String login;

    @Pattern(regexp = "\\d{10}", message = "Le numéro de téléphone doit contenir exactement 10 chiffres")
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private SystemConnected systemConnected;

    private Long failedAttempt;

    private Date blockDate;
}
