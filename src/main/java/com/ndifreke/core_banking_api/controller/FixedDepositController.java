package com.ndifreke.core_banking_api.controller;

import com.ndifreke.core_banking_api.service.savings.FixedDepositService;
import com.ndifreke.core_banking_api.dto.savings.FixedDepositRequest;
import com.ndifreke.core_banking_api.dto.savings.FixedDepositResponse;
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
 * The type Fixed deposit controller.
 */
@RestController
@RequestMapping("/api/fixed-deposits")
@Tag(name = "Fixed Deposits", description = "Endpoints for managing fixed deposits")
@Validated
public class FixedDepositController {

    @Autowired
    private FixedDepositService fixedDepositService;

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
     * Create a new fixed deposit.
     */
    @Operation(summary = "Create a new fixed deposit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fixed deposit created successfully",
                    content = @Content(schema = @Schema(implementation = FixedDepositResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (e.g., invalid input)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (e.g., insufficient permissions)",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createFixedDeposit(
            @Valid @RequestBody FixedDepositRequest fixedDepositRequest,
            HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            FixedDepositResponse response = fixedDepositService.createFixedDeposit(fixedDepositRequest, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResponseStatusException e) {
            return createErrorResponse((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    /**
     * Get all fixed deposits for the authenticated user.
     */
    @Operation(summary = "Get all fixed deposits for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fixed deposits retrieved successfully",
                    content = @Content(schema = @Schema(implementation = FixedDepositResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (e.g., insufficient permissions)",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getFixedDeposits(HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            List<FixedDepositResponse> response = fixedDepositService.getFixedDeposits(userId);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return createErrorResponse((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    /**
     * Get a fixed deposit by ID.
     */
    @Operation(summary = "Get a fixed deposit by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fixed deposit retrieved successfully",
                    content = @Content(schema = @Schema(implementation = FixedDepositResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (e.g., access denied)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Fixed deposit not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/{depositId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getFixedDepositById(
            @PathVariable UUID depositId,
            HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            FixedDepositResponse response = fixedDepositService.getFixedDepositById(depositId, userId);
            if (response == null) {
                return createErrorResponse(HttpStatus.NOT_FOUND, "Fixed deposit not found");
            }
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return createErrorResponse((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }

    /**
     * Withdraw funds from a fixed deposit.
     */
    @Operation(summary = "Withdraw funds from a fixed deposit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds withdrawn successfully",
                    content = @Content(schema = @Schema(implementation = FixedDepositResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (e.g., invalid state)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (e.g., access denied)",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "Fixed deposit not found",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "409", description = "Conflict (e.g., deposit not matured)",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/{depositId}/withdraw")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> withdrawFixedDeposit(
            @PathVariable UUID depositId,
            HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        try {
            FixedDepositResponse response = fixedDepositService.withdrawFixedDeposit(depositId, userId);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return createErrorResponse((HttpStatus) e.getStatusCode(), e.getReason());
        }
    }
}