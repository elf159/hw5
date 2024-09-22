package com.example.accounts.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionDTO {
    String transactionId;
    BigDecimal amount;

}
