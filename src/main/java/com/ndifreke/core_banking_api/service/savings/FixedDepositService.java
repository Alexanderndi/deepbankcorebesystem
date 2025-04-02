package com.ndifreke.core_banking_api.service.savings;

import com.ndifreke.core_banking_api.service.savings.dtos.FixedDepositRequest;
import com.ndifreke.core_banking_api.service.savings.dtos.FixedDepositResponse;
import com.ndifreke.core_banking_api.service.savings.enums.FixedDepositStatus;
import com.ndifreke.core_banking_api.service.savings.repository.FixedDepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The type Fixed deposit service.
 */
@Service
public class FixedDepositService {

    @Autowired
    private FixedDepositRepository fixedDepositRepository;

    /**
     * Create fixed deposit fixed deposit response.
     *
     * @param request the request
     * @param userId  the user id
     * @return the fixed deposit response
     */
    public FixedDepositResponse createFixedDeposit(FixedDepositRequest request, UUID userId) {
        validateFixedDepositRequest(request);

        FixedDeposit fixedDeposit = new FixedDeposit();
        fixedDeposit.setUserId(userId);
        fixedDeposit.setDepositAmount(request.getDepositAmount());
        fixedDeposit.setDepositDate(request.getDepositDate());
        fixedDeposit.setMaturityDate(request.getMaturityDate());
        fixedDeposit.setInterestRate(request.getInterestRate());
        fixedDeposit.setStatus(FixedDepositStatus.ACTIVE);

        FixedDeposit savedDeposit = fixedDepositRepository.save(fixedDeposit);
        return convertToFixedDepositResponse(savedDeposit);
    }

    /**
     * Gets fixed deposits.
     *
     * @param userId the user id
     * @return the fixed deposits
     */
    public List<FixedDepositResponse> getFixedDeposits(UUID userId) {
        List<FixedDeposit> fixedDeposits = fixedDepositRepository.findByUserId(userId);
        return fixedDeposits.stream().map(this::convertToFixedDepositResponse).collect(Collectors.toList());
    }

    /**
     * Gets fixed deposit by id.
     *
     * @param depositId the deposit id
     * @param userId    the user id
     * @return the fixed deposit by id
     */
    public FixedDepositResponse getFixedDepositById(UUID depositId, UUID userId) {
        FixedDeposit fixedDeposit = fixedDepositRepository.findById(depositId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fixed deposit not found"));
        if (!fixedDeposit.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return convertToFixedDepositResponse(fixedDeposit);
    }

    /**
     * Withdraw fixed deposit fixed deposit response.
     *
     * @param depositId the deposit id
     * @param userId    the user id
     * @return the fixed deposit response
     */
    public FixedDepositResponse withdrawFixedDeposit(UUID depositId, UUID userId) {
        FixedDeposit fixedDeposit = fixedDepositRepository.findById(depositId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fixed deposit not found"));
        if (!fixedDeposit.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        if (fixedDeposit.getStatus() != FixedDepositStatus.MATURED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Fixed deposit not matured");
        }
        fixedDeposit.setStatus(FixedDepositStatus.CLOSED);
        fixedDepositRepository.save(fixedDeposit);
        return convertToFixedDepositResponse(fixedDeposit);
    }

    private FixedDepositResponse convertToFixedDepositResponse(FixedDeposit fixedDeposit) {
        FixedDepositResponse response = new FixedDepositResponse();
        response.setDepositId(fixedDeposit.getDepositId());
        response.setDepositAmount(fixedDeposit.getDepositAmount());
        response.setDepositDate(fixedDeposit.getDepositDate());
        response.setMaturityDate(fixedDeposit.getMaturityDate());
        response.setInterestRate(fixedDeposit.getInterestRate());
        response.setStatus(fixedDeposit.getStatus().toString());
        return response;
    }

    private void validateFixedDepositRequest(FixedDepositRequest request) {
        if (request.getDepositAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deposit amount must be greater than zero");
        }
        if (request.getDepositDate().isAfter(request.getMaturityDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deposit date must be before maturity date");
        }
        if (request.getInterestRate().compareTo(BigDecimal.ZERO) < 0 || request.getInterestRate().compareTo(BigDecimal.ONE) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interest rate must be between 0 and 1");
        }
    }
}