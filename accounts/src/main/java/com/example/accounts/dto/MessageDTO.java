package com.example.accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class MessageDTO {

    private Integer customerId;

    private String message;
}
