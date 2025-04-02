package com.ndifreke.core_banking_api.security.controller;

import com.ndifreke.core_banking_api.security.entity.UserDto;
import com.ndifreke.core_banking_api.user.User;
import com.ndifreke.core_banking_api.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Admin controller.
 */
@RestController
public class AdminController {
    private final UserRepository userRepository;

    /**
     * Instantiates a new Admin controller.
     *
     * @param userRepository the user repository
     */
    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Gets admin data.
     *
     * @return the admin data
     */
    @GetMapping("/admin/data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAdminData() {
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Gets teller data.
     *
     * @return the teller data
     */
    @GetMapping("/teller/data")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    public String getTellerData() {
        return "Teller data";
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId().toString()); // Assuming userId is UUID
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRoles(user.getRoles().stream()
                .map(Enum::name) // Convert UserRole enum to String
                .collect(Collectors.toList()));
        return dto;
    }
}