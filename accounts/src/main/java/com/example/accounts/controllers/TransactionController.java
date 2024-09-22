package com.example.accounts.controllers;

import com.example.accounts.dto.TransactionDTO;
import com.example.accounts.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @GetMapping("/accounts/{accountNumber}/transactions")
    public ResponseEntity<List<TransactionDTO>> getTransactions(@PathVariable(value = "accountNumber") Integer accountNumber) {
        return transactionService.getTransactions(accountNumber);
    }
}
