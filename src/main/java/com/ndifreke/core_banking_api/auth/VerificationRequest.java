package com.ndifreke.core_banking_api.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Verification Request")
public class VerificationRequest {

    private String username;
    private String verificationCode;
}
