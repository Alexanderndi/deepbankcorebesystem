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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * The type Fixed deposit controller.
 */
@RestController
@RequestMapping("/api/fixed-deposits")
@Tag(name = "Fixed Deposits", description = "Endpoints for managing fixed deposits")
public class FixedDepositController {

    @Autowired
    private FixedDepositService fixedDepositService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Create fixed deposit response entity.
     *
     * @param fixedDepositRequest the fixed deposit request
     * @param request             the request
     * @return the response entity
     */
    @Operation(summary = "Create a new fixed deposit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fixed deposit created successfully", content = @Content(schema = @Schema(implementation = FixedDepositResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FixedDepositResponse> createFixedDeposit(
            @RequestBody FixedDepositRequest fixedDepositRequest,
            HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        FixedDepositResponse response = fixedDepositService.createFixedDeposit(fixedDepositRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets fixed deposits.
     *
     * @param request the request
     * @return the fixed deposits
     */
    @Operation(summary = "Get all fixed deposits for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fixed deposits retrieved successfully", content = @Content(schema = @Schema(implementation = FixedDepositResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<FixedDepositResponse>> getFixedDeposits(HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        List<FixedDepositResponse> response = fixedDepositService.getFixedDeposits(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets fixed deposit by id.
     *
     * @param depositId the deposit id
     * @param request   the request
     * @return the fixed deposit by id
     */
    @Operation(summary = "Get a fixed deposit by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fixed deposit retrieved successfully", content = @Content(schema = @Schema(implementation = FixedDepositResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Fixed deposit not found", content = @Content)
    })
    @GetMapping("/{depositId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FixedDepositResponse> getFixedDepositById(
            @PathVariable UUID depositId,
            HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        FixedDepositResponse response = fixedDepositService.getFixedDepositById(depositId, userId);
        return response == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(response);
    }

    /**
     * Withdraw fixed deposit response entity.
     *
     * @param depositId the deposit id
     * @param request   the request
     * @return the response entity
     */
    @Operation(summary = "Withdraw funds from a fixed deposit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds withdrawn successfully", content = @Content(schema = @Schema(implementation = FixedDepositResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Fixed deposit not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict (e.g., early withdrawal penalty)", content = @Content)
    })
    @PostMapping("/{depositId}/withdraw")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FixedDepositResponse> withdrawFixedDeposit(
            @PathVariable UUID depositId,
            HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        FixedDepositResponse response = fixedDepositService.withdrawFixedDeposit(depositId, userId);
        return ResponseEntity.ok(response);
    }
}