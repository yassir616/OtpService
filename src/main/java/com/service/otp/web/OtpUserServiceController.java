package com.service.otp.web;

import com.service.otp.models.UserOtp;
import com.service.otp.requestModels.CreateUserRequestModel;
import com.service.otp.services.UserOtpService;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-service")
public class OtpUserServiceController {

    private final UserOtpService userOtpService;

    public OtpUserServiceController(UserOtpService userOtpService) {
        this.userOtpService = userOtpService;
    }

    @PostMapping("/findOrCreateUser")
    public ResponseEntity<?> findOrCreateUser(@Valid @RequestBody CreateUserRequestModel requestModel,
            @RequestParam String systemName) {
        try {
            UserOtp user = userOtpService.findOrCreateUser(requestModel, systemName);
            return ResponseEntity.ok(user);
        } catch (ConstraintViolationException ex) {
            String errorMessage = ex.getConstraintViolations().stream()
                    .map(violation -> violation.getMessage())
                    .findFirst()
                    .orElse("Validation error occurred");
            return ResponseEntity.badRequest().body(errorMessage);
        }
    }
}
