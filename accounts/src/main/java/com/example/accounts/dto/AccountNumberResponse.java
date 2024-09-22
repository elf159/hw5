package com.example.accounts.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountNumberResponse {
    private String currency;
    private BigDecimal amount;
}
