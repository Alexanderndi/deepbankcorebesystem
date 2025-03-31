package com.ndifreke.core_banking_api.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Registration Request")
public class RegisterRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private List<String> roles;
}