package com.ndifreke.core_banking_api.transaction.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Transaction history response object")
public class TransactionHistoryResponse {

    private List<TransactionResponseInterface> transactions;

    // Getters and setters...

    public List<TransactionResponseInterface> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionResponseInterface> transactions) {
        this.transactions = transactions;
    }
}