package com.ndifreke.core_banking_api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Authentication request.
 */
@Getter
@Setter
@Schema(description = "Authentication Request")
public class AuthenticationRequest {
    private String username;
    private String password;
}
