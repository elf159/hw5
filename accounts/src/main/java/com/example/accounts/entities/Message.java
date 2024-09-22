package com.example.accounts.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "account_number")
    private Integer accountNumber;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "balance")
    private BigDecimal balance;
}
