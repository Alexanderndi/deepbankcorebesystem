package com.ndifreke.core_banking_api.dto.transaction;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * The type Transaction history response.
 */
@Schema(description = "Transaction history response object")
public class TransactionHistoryResponse {

    private List<TransactionResponseInterface> transactions;

    // Getters and setters...

    /**
     * Gets transactions.
     *
     * @return the transactions
     */
    public List<TransactionResponseInterface> getTransactions() {
        return transactions;
    }

    /**
     * Sets transactions.
     *
     * @param transactions the transactions
     */
    public void setTransactions(List<TransactionResponseInterface> transactions) {
        this.transactions = transactions;
    }
}