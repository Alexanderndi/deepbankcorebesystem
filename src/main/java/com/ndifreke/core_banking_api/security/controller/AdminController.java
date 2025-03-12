package com.ndifreke.core_banking_api.security.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @GetMapping("/admin/data")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAdminData() {
        return "Admin data";
    }

    @GetMapping("/teller/data")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    public String getTellerData() {
        return "Teller data";
    }

    @GetMapping("/user/data")
    @PreAuthorize("hasRole('USER')")
    public String getUserData() { return "User Data"; }
}