package com.example.steamlensbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SteamLensBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SteamLensBackendApplication.class, args);
	}

}
