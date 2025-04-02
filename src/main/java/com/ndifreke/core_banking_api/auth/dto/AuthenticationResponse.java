package com.ndifreke.core_banking_api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * The type Authentication response.
 */
@Getter
@Schema(description = "Authentication Response")
public class AuthenticationResponse {
    private final String jwt;

    /**
     * Instantiates a new Authentication response.
     *
     * @param jwt the jwt
     */
    public AuthenticationResponse(String jwt) {
        this.jwt = jwt;
    }
}
