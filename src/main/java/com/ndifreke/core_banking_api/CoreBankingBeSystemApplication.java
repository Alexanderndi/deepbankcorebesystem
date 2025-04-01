package com.ndifreke.core_banking_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CoreBankingBeSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreBankingBeSystemApplication.class, args);
	}

}
