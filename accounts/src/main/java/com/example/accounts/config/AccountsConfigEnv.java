package com.example.accounts.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
@Getter
public class AccountsConfigEnv {
    HashSet<String> allowedCurrencies = new HashSet<>(Set.of("RUB", "USD", "GBP", "EUR", "CYN"));
    @Value("${CONVERTER_URL}")
    String CONVERTER_URL;
    @Value("${KEYCLOAK_URL}")
    String KEYCLOAK_URL;
    @Value("${KEYCLOAK_REALM}")
    String KEYCLOAK_REALM;
    @Value("${CLIENT_ID}")
    String client_id;
    @Value("${CLIENT_SECRET}")
    String client_secret;
}
