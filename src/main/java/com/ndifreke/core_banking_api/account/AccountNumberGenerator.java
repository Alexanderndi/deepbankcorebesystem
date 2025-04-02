package com.ndifreke.core_banking_api.account;

import java.util.UUID;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The type Account number generator.
 */
public class AccountNumberGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    /**
     * Generate account number string.
     *
     * @return the string
     */
    public static String generateAccountNumber() {
        // Combine timestamp, random numbers, and a prefix.
        String timestamp = LocalDateTime.now().format(dateTimeFormatter);
        String randomPart = generateRandomNumberString(4); // 4 digits random part
        String prefix = "ACC";

        return prefix + timestamp + randomPart;
    }

    private static String generateRandomNumberString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(secureRandom.nextInt(10)); // Generate a random digit (0-9)
        }
        return sb.toString();
    }

    /**
     * Generate uuid based account number string.
     *
     * @return the string
     */
// A more robust method that creates a UUID based account number.
    public static String generateUUIDBasedAccountNumber(){
        String uuidString = UUID.randomUUID().toString().replace("-","");
        return "ACC-"+uuidString.substring(0,16).toUpperCase();
    }

    /**
     * Generate timestamp uuid account number string.
     *
     * @return the string
     */
// A method that combines a timestamp and a UUID.
    public static String generateTimestampUUIDAccountNumber(){
        String timestamp = LocalDateTime.now().format(dateTimeFormatter);
        String uuidString = UUID.randomUUID().toString().replace("-","");
        return "ACC-"+timestamp+uuidString.substring(0,8).toUpperCase();
    }
}