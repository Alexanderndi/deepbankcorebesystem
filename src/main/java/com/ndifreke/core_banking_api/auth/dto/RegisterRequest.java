package com.ndifreke.core_banking_api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Username for the new user", example = "johndoe123")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Schema(description = "Password for the new user", example = "StrongPass123!")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Schema(description = "First name of the user", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Schema(description = "Last name of the user", example = "Doe")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email address of the user", example = "johndoe@example.com")
    private String email;

    @Schema(description = "List of user roles", example = "[\"USER\"]")
    private List<String> roles;


    public List<String> getRoles() {
        return roles != null ? new ArrayList<>(roles) : new ArrayList<>();
    }

    public void setRoles(List<String> roles) {
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
    }
}