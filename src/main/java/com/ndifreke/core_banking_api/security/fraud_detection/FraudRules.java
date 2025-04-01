package com.ndifreke.core_banking_api.security.fraud_detection;

import java.math.BigDecimal;

public class FraudRules {
    public static final BigDecimal LARGE_TRANSFER_THRESHOLD = new BigDecimal("500000.00");
    public static final int HIGH_FREQUENCY_TRANSACTION_LIMIT = 5; // Transactions within a timeframe
    public static final int HIGH_FREQUENCY_TIMEFRAME_MINUTES = 10;
    public static final String[] BLACKLISTED_ACCOUNTS = {"ACC-12345", "ACC-67890"};
    public static final BigDecimal HIGH_VELOCITY_BALANCE_CHANGE = new BigDecimal("0.8");
}
