package com.ndifreke.core_banking_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * The type Core banking be system application.
 */
@SpringBootApplication
@EnableCaching
public class CoreBankingBeSystemApplication {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
		SpringApplication.run(CoreBankingBeSystemApplication.class, args);
	}

}
