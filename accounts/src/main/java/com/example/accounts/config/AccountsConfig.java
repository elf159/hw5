package com.example.accounts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AccountsConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
