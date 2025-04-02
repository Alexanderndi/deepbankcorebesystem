package com.ndifreke.core_banking_api.service.savings;

import com.ndifreke.core_banking_api.service.savings.dtos.SavingsPlanDepositRequest;
import com.ndifreke.core_banking_api.service.savings.dtos.SavingsPlanRequest;
import com.ndifreke.core_banking_api.service.savings.dtos.SavingsPlanResponse;
import com.ndifreke.core_banking_api.service.savings.dtos.SavingsPlanWithdrawalRequest;
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
 * The type Savings plan controller.
 */
@RestController
@RequestMapping("/api/savings-plans")
@Tag(name = "Savings Plans", description = "Endpoints for managing savings plans")
public class SavingsPlanController {

    @Autowired
    private SavingsPlanService savingsPlanService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Create savings plan response entity.
     *
     * @param savingsPlanRequest the savings plan request
     * @param request            the request
     * @return the response entity
     */
    @Operation(summary = "Create a new savings plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Savings plan created successfully", content = @Content(schema = @Schema(implementation = SavingsPlanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SavingsPlanResponse> createSavingsPlan(
            @RequestBody SavingsPlanRequest savingsPlanRequest,
            HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        SavingsPlanResponse response = savingsPlanService.createSavingsPlan(savingsPlanRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets savings plans.
     *
     * @param request the request
     * @return the savings plans
     */
    @Operation(summary = "Get all savings plans for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Savings plans retrieved successfully", content = @Content(schema = @Schema(implementation = SavingsPlanResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<SavingsPlanResponse>> getSavingsPlans(HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        List<SavingsPlanResponse> response = savingsPlanService.getSavingsPlans(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets savings plan by id.
     *
     * @param planId  the plan id
     * @param request the request
     * @return the savings plan by id
     */
    @Operation(summary = "Get a savings plan by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Savings plan retrieved successfully", content = @Content(schema = @Schema(implementation = SavingsPlanResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Savings plan not found", content = @Content)
    })
    @GetMapping("/{planId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SavingsPlanResponse> getSavingsPlanById(
            @PathVariable UUID planId,
            HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        SavingsPlanResponse response = savingsPlanService.getSavingsPlanById(planId, userId);
        return response == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(response);
    }

    /**
     * Deposit to savings plan response entity.
     *
     * @param planId         the plan id
     * @param depositRequest the deposit request
     * @param request        the request
     * @return the response entity
     */
    @Operation(summary = "Deposit funds into a savings plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds deposited successfully", content = @Content(schema = @Schema(implementation = SavingsPlanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Savings plan not found", content = @Content)
    })
    @PostMapping("/{planId}/deposit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SavingsPlanResponse> depositToSavingsPlan(
            @PathVariable UUID planId,
            @RequestBody SavingsPlanDepositRequest depositRequest,
            HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        SavingsPlanResponse response = savingsPlanService.depositToSavingsPlan(planId, depositRequest.getAmount(), userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Withdraw from savings plan response entity.
     *
     * @param planId            the plan id
     * @param withdrawalRequest the withdrawal request
     * @param request           the request
     * @return the response entity
     */
    @Operation(summary = "Withdraw funds from a savings plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Funds withdrawn successfully", content = @Content(schema = @Schema(implementation = SavingsPlanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Savings plan not found", content = @Content)
    })
    @PostMapping("/{planId}/withdraw")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SavingsPlanResponse> withdrawFromSavingsPlan(
            @PathVariable UUID planId,
            @RequestBody SavingsPlanWithdrawalRequest withdrawalRequest,
            HttpServletRequest request) {
        UUID userId = jwtUtil.extractUserId(jwtUtil.getTokenFromRequest(request));
        SavingsPlanResponse response = savingsPlanService.withdrawFromSavingsPlan(planId, withdrawalRequest.getAmount(), userId);
        return ResponseEntity.ok(response);
    }
}
