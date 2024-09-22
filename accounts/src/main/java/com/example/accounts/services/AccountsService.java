package com.example.accounts.services;

import com.example.accounts.config.AccountsConfigEnv;
import com.example.accounts.dto.*;
import com.example.accounts.entities.Account;
import com.example.accounts.entities.Message;
import com.example.accounts.repositories.AccountsRepository;
import com.example.accounts.repositories.OutboxMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class AccountsService {
    private final AccountsRepository accountsRepository;
    private final AccountsConfigEnv accountsConfigEnv;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final OutboxMessageRepository outboxMessageRepository;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, AccountsConfigEnv accountsConfigEnv,
                           SimpMessagingTemplate simpMessagingTemplate, OutboxMessageRepository outboxMessageRepository) {
        this.accountsRepository = accountsRepository;
        this.accountsConfigEnv = accountsConfigEnv;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.outboxMessageRepository = outboxMessageRepository;
    }
    int accountNumber = 1;
    private Integer createAccountNumber() {
        while (accountsRepository.findAccountByAccountNumber(accountNumber).isPresent()) {
            accountNumber++;
        }
        return accountNumber;
    }
    public ResponseEntity<CreateAccountResponse> createCustomerAccount(AccountDTO accountDTO) {
        if (checkAccountDTO(accountDTO)) {
            return ResponseEntity.badRequest().build();
        }
        if (findCustomerById(accountDTO)) {
            return ResponseEntity.badRequest().build();
        }
        Account account = new Account();
        account.setAccountNumber(createAccountNumber());
        account.setCurrency(accountDTO.getCurrency());
        account.setBalance(BigDecimal.ZERO);
        account.setCustomerId(accountDTO.getCustomerId());

        CreateAccountResponse createAccountResponse = new CreateAccountResponse();
        createAccountResponse.setAccountNumber(account.getAccountNumber());
        accountsRepository.save(account);

        AccountSocketDTO accountSocketDTO = new AccountSocketDTO();
        accountSocketDTO.setAccountNumber(account.getAccountNumber());
        accountSocketDTO.setCurrency(account.getCurrency());
        accountSocketDTO.setBalance(account.getBalance());

        simpMessagingTemplate.convertAndSend("/topic/accounts", accountSocketDTO);

        return ResponseEntity.status(HttpStatus.OK).body(createAccountResponse);
    }
    private boolean checkAccountDTO(AccountDTO accountDTO) {
        return accountDTO.getCustomerId() == null || accountDTO.getCustomerId().toString().isEmpty() || accountDTO.getCurrency() == null
                || accountDTO.getCurrency().isEmpty() || !accountDTO.getCustomerId().toString().matches("\\d+")
                || !accountsConfigEnv.getAllowedCurrencies().contains(accountDTO.getCurrency());
    }
    private boolean findCustomerById(AccountDTO accountDTO) {
        return accountsRepository.findAccountByCustomerId(accountDTO.getCustomerId()).stream()
                .anyMatch(x -> x.getCurrency().equals(accountDTO.getCurrency()));
    }

    public ResponseEntity<AccountNumberResponse> findByAccNumber(Integer accNum) {
        if (checkAccNum(accNum)) {
            return ResponseEntity.badRequest().build();
        }
        return processFindByAccNumber(accNum);
    }
    private ResponseEntity<AccountNumberResponse> processFindByAccNumber(Integer accNum) {
        AccountNumberResponse accountNumberResponse = new AccountNumberResponse();
        Optional<Account> account = accountsRepository.findAccountByAccountNumber(accNum);
        if (account.isPresent()) {
            accountNumberResponse.setAmount(account.get().getBalance());
            accountNumberResponse.setCurrency(account.get().getCurrency());
        } else {
            return ResponseEntity.badRequest().build();
        }
        if (accountNumberResponse.getCurrency().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(accountNumberResponse);
    }
    private boolean checkAccNum(Integer accNum) {
        return accNum == null || accNum.toString().isEmpty() || accNum == 0;
    }
    public ResponseEntity<?> topUpAccount(Integer accNum, AmountDTO amountDTO) {
        if (checkAccNum(accNum)) {
            return ResponseEntity.badRequest().build();
        }
        if (amountDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return processTopUpAccount(accNum, amountDTO);
    }

    private ResponseEntity<?> processTopUpAccount(Integer accNum, AmountDTO amountDTO) {
        Optional<Account> account = accountsRepository.findAccountByAccountNumber(accNum);
        if (account.isPresent()) {
            Account getAccount = account.get();
            getAccount.setBalance(getAccount.getBalance().add(amountDTO.getAmount()));
            accountsRepository.save(getAccount);
            AccountSocketDTO accountSocketDTO = new AccountSocketDTO();
            accountSocketDTO.setAccountNumber(getAccount.getAccountNumber());
            accountSocketDTO.setCurrency(getAccount.getCurrency());
            accountSocketDTO.setBalance(getAccount.getBalance());
            simpMessagingTemplate.convertAndSend("/topic/accounts", accountSocketDTO);
            Message message = new Message();
            message.setAccountNumber(getAccount.getAccountNumber());
            message.setAmount(amountDTO.getAmount());
            message.setBalance(getAccount.getBalance());
            outboxMessageRepository.save(message);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
