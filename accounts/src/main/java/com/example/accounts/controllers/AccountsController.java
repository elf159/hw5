package com.example.accounts.controllers;

import com.example.accounts.dto.*;
import com.example.accounts.services.AccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class AccountsController {
    AccountsService accountsService;
    @Autowired
    public AccountsController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<CreateAccountResponse> createCustomerAccount(@RequestBody AccountDTO accountDTO) {
        return accountsService.createCustomerAccount(accountDTO);
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<AccountNumberResponse> findByAccNumber(@PathVariable(value = "accountNumber") Integer accNum) {
        return accountsService.findByAccNumber(accNum);
    }

    @PostMapping("/accounts/{accountNumber}/top-up")
    public ResponseEntity<TransactionDTO> topUpAccount(@RequestHeader(value = "Idempotency-Key") String idempotencyKey, @PathVariable(value = "accountNumber") Integer accNum, @RequestBody AmountDTO amountDTO) {
        return accountsService.topUpAccount(idempotencyKey, accNum, amountDTO);
    }
}
