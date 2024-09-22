package com.example.accounts.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class ConvertResponse {
    private String currency;
    private BigDecimal amount;
}
