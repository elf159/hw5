package com.example.accounts.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class TransferDTO {

    Integer receiverAccount;
    Integer senderAccount;
    BigDecimal amountInSenderCurrency;
}
