package com.ndifreke.core_banking_api.controller;

import com.ndifreke.core_banking_api.account.AccountService;
import com.ndifreke.core_banking_api.service.savings.SavingsPlanService;
import com.ndifreke.core_banking_api.dto.savings.SavingsPlanDepositRequest;
import com.ndifreke.core_banking_api.dto.savings.SavingsPlanRequest;
import com.ndifreke.core_banking_api.dto.savings.SavingsPlanResponse;
import com.ndifreke.core_banking_api.dto.savings.SavingsPlanWithdrawalRequest;
import com.ndifreke.core_banking_api.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The type Savings plan controller.
 */
@RestController
@RequestMapping("/api/savings-plans")
@Tag(name = "Savings Plans", description = "Endpoints for managing savings plans")
@Validated
public class SavingsPlanController {

    @Autowired
    private SavingsPlanService savingsPlanService;

    @Autowired
    private JwtUtil jwtUtil;

    // Helper method to create error response
    private ResponseEntity<Map<String, String>> createErrorResponse(HttpStatus status, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Create savings plan.
     */
    @Operation(summary = "Create a new savings plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Savings plan created successfully",
                    content = @Content(schema = @Schema(implementation = SavingsPlanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (e.g., invalid input)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (e.g., insufficient permissions)",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createSavingsPlan(
            @Valid @RequestBody SavingsPlanRequest savingsPlanRequest,
            HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            SavingsPlanResponse response = savingsPlanService.createSavingsPlan(savingsPlanRequest, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResponseStatusException e) {
            return createErrorResponse((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    /**
     * Get all savings plans for the authenticated user.
     */
    @Operation(summary = "Get all savings plans for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Savings plans retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SavingsPlanResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (e.g., insufficient permissions)",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getSavingsPlans(HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            List<SavingsPlanResponse> response = savingsPlanService.getSavingsPlans(userId);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return createErrorResponse((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    /**
     * Get a savings plan by ID.
     */
    @Operation(summary = "Get a savings plan by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Savings plan retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SavingsPlanResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (e.g., access denied)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Savings plan not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/{planId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getSavingsPlanById(
            @PathVariable UUID planId,
            HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            SavingsPlanResponse response = savingsPlanService.getSavingsPlanById(planId, userId);
            if (response == null) {
                return createErrorResponse(HttpStatus.NOT_FOUND, "Savings plan not found");
            }
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return createErrorResponse((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    /**
     * Deposit funds into a savings plan.
     */
    @Operation(summary = "Deposit funds into a savings plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds deposited successfully",
                    content = @Content(schema = @Schema(implementation = SavingsPlanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (e.g., invalid amount)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (e.g., access denied)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Savings plan not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/{planId}/deposit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> depositToSavingsPlan(
            @PathVariable UUID planId,
            @Valid @RequestBody SavingsPlanDepositRequest depositRequest,
            HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            SavingsPlanResponse response = savingsPlanService.depositToSavingsPlan(planId, depositRequest.getAmount(), userId);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return createErrorResponse((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    /**
     * Withdraw funds from a savings plan.
     */
    @Operation(summary = "Withdraw funds from a savings plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds withdrawn successfully",
                    content = @Content(schema = @Schema(implementation = SavingsPlanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (e.g., invalid amount, insufficient balance)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (e.g., access denied)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Savings plan not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/{planId}/withdraw")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> withdrawFromSavingsPlan(
            @PathVariable UUID planId,
            @Valid @RequestBody SavingsPlanWithdrawalRequest withdrawalRequest,
            HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            SavingsPlanResponse response = savingsPlanService.withdrawFromSavingsPlan(planId, withdrawalRequest.getAmount(), userId);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return createErrorResponse((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }
}