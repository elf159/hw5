package com.example.accounts.services;

import com.example.accounts.config.AccountsConfigEnv;
import com.example.accounts.dto.*;
import com.example.accounts.entities.Account;
import com.example.accounts.repositories.AccountsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class AccountsService {
    private final AccountsRepository accountsRepository;
    private final AccountsConfigEnv accountsConfigEnv;

    private final OutBoxService outboxService;
    private final RedisService redisService;
    private final TransactionService transactionService;
    private final SocketService socketService;
    @Autowired
    public AccountsService(AccountsRepository accountsRepository, AccountsConfigEnv accountsConfigEnv,
                           OutBoxService outboxService, RedisService redisService,
                           TransactionService transactionService, SocketService socketService) {
        this.accountsRepository = accountsRepository;
        this.accountsConfigEnv = accountsConfigEnv;
        this.outboxService = outboxService;
        this.redisService = redisService;
        this.transactionService = transactionService;
        this.socketService = socketService;
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

        socketService.send(account);

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
    public ResponseEntity<TransactionDTO> topUpAccount(String key, Integer accNum, AmountDTO amountDTO) {
        if (checkAccNum(accNum)) {
            return ResponseEntity.badRequest().build();
        }
        if (amountDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().build();
        }
        if (key != null) {
            if (redisService.contain(key)) {
                return ResponseEntity.ok(redisService.getCache(key));
            }
        }
        return processTopUpAccount(key, accNum, amountDTO);
    }

    private ResponseEntity<TransactionDTO> processTopUpAccount(String key, Integer accNum, AmountDTO amountDTO) {
        Optional<Account> account = accountsRepository.findAccountByAccountNumber(accNum);
        if (account.isPresent()) {
            Account getAccount = account.get();
            getAccount.setBalance(getAccount.getBalance().add(amountDTO.getAmount()));
            accountsRepository.save(getAccount);
            socketService.send(getAccount);

            outboxService.save(getAccount, amountDTO.getAmount());


            TransactionDTO transaction = new TransactionDTO();
            transaction.setTransactionId(String.valueOf(UUID.randomUUID()));
            transaction.setAmount(amountDTO.getAmount());

            if (key != null) {
                redisService.saveCache(key, transaction);
            }

            transactionService.save(transaction, accNum, amountDTO.getAmount());
            return ResponseEntity.ok(transaction);
        }
        return ResponseEntity.badRequest().build();
    }
}
