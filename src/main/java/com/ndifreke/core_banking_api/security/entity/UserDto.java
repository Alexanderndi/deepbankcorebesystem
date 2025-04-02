package com.ndifreke.core_banking_api.security.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The type User dto.
 */
@Getter
@Setter
@Data
public class UserDto {
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
}