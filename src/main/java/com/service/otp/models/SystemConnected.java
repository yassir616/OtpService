package com.service.otp.models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemConnected extends AbstractBaseEntity {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "System name cannot be null")
    private String systemName;

    @JsonIgnore
    @OneToMany(mappedBy = "systemConnected")
    private List<UserOtp> users;
}
