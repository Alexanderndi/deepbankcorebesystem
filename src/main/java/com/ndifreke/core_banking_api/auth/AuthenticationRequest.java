package com.ndifreke.core_banking_api.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Authentication Request")
public class AuthenticationRequest {
    private String username;
    private String password;
}
