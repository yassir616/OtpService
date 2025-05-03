package com.service.otp.web;

import com.service.otp.models.UserOtp;
import com.service.otp.requestModels.CreateUserRequestModel;
import com.service.otp.services.UserOtpService;
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
    public ResponseEntity<UserOtp> findOrCreateUser(@RequestBody CreateUserRequestModel requestModel, 
                                                    @RequestParam String systemName) {
        UserOtp user = userOtpService.findOrCreateUser(requestModel,systemName);
        return ResponseEntity.ok(user);
    }
}
