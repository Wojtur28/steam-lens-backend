package com.example.steamlensbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@ConfigurationPropertiesScan("com.example.steamlensbackend.config.properties")
public class SteamLensBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SteamLensBackendApplication.class, args);
	}

}
