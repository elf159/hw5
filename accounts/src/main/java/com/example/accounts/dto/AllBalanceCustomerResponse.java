package com.example.accounts.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class AllBalanceCustomerResponse {
    private BigDecimal balance;
    private String currency;
}
