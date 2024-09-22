package com.example.accounts.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number")
    private Integer accountNumber;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "customer_id")
    private Integer customerId;
}
