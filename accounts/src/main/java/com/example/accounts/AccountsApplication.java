package com.example.accounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.example.accounts.entities")
@EnableJpaRepositories(basePackages = {"com.example.accounts.repositories"})
@ComponentScan(basePackages = {"com.example.accounts.config"})
@ComponentScan("com.example.accounts.controllers")
@ComponentScan("com.example.accounts.services")
public class AccountsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountsApplication.class, args);
    }
}
