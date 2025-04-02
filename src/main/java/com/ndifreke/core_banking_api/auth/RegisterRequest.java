package com.ndifreke.core_banking_api.auth;

import lombok.Data;
import java.util.List;

/**
 * The type Register request.
 */
@Data
public class RegisterRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private List<String> roles;
}