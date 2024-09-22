package com.example.accounts.repositories;

import com.example.accounts.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionsRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findAllByAccountNumber(Integer accountNumber);
}
