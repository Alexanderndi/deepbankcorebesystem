package com.ndifreke.core_banking_api.security.fraud_detection;

import java.math.BigDecimal;

/**
 * The type Fraud rules.
 */
public class FraudRules {
    /**
     * The constant LARGE_TRANSFER_THRESHOLD.
     */
    public static final BigDecimal LARGE_TRANSFER_THRESHOLD = new BigDecimal("500000.00");
    /**
     * The constant HIGH_FREQUENCY_TRANSACTION_LIMIT.
     */
    public static final int HIGH_FREQUENCY_TRANSACTION_LIMIT = 5; // Transactions within a timeframe
    /**
     * The constant HIGH_FREQUENCY_TIMEFRAME_MINUTES.
     */
    public static final int HIGH_FREQUENCY_TIMEFRAME_MINUTES = 10;
//    /**
//     * The constant BLACKLISTED_ACCOUNTS.
//     */
//    public static final String[] BLACKLISTED_ACCOUNTS = {"ACC-12345", "ACC-67890"};
//    /**
//     * The constant HIGH_VELOCITY_BALANCE_CHANGE.
//     */
//    public static final BigDecimal HIGH_VELOCITY_BALANCE_CHANGE = new BigDecimal("0.8");
}
