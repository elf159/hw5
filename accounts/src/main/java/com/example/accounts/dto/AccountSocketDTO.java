package com.example.accounts.dto;

import com.example.accounts.config.DecimalSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountSocketDTO {
    Integer accountNumber;
    String currency;

    @JsonSerialize(using = DecimalSerialize.class)
    BigDecimal balance;
}
