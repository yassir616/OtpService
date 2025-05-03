package com.service.otp.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.service.otp.requestModels.CreateOtpCodeRequestModel;
import com.service.otp.requestModels.ValidateOtpRequestModel;
import com.service.otp.services.OtpCodeService;

@RestController
public class OtpCodeServiceController {

    @Autowired
    private OtpCodeService otpCodeService;

    @PostMapping(path = "/gererOtp")
    public ResponseEntity<String> genererOtp(@RequestBody CreateOtpCodeRequestModel requestModel) {
        try {
            String otp = otpCodeService.genererOtp(requestModel);
            return ResponseEntity.ok(otp);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // HTTP 400 Bad Request
        }
    }

    @PostMapping(path = "/validateOtp")
    public ResponseEntity<String> validateOtp(@RequestBody ValidateOtpRequestModel requestModel) {
        try {
            otpCodeService.validateOtp(
                requestModel.getCodeValue(),
                requestModel.getSystemName(),
                requestModel.getUserLogin()
            );
            return ResponseEntity.ok("OTP valide");
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Code Expiré")) {
                return ResponseEntity.status(HttpStatus.GONE).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
