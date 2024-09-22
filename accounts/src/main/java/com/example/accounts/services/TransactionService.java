package com.example.accounts.services;

import com.example.accounts.dto.TransactionDTO;
import com.example.accounts.entities.Transaction;
import com.example.accounts.repositories.TransactionsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final ObjectMapper objectMapper;
    private final TransactionsRepository transactionsRepository;

    public TransactionService(TransactionsRepository transactionsRepository, ObjectMapper objectMapper) {
        this.transactionsRepository = transactionsRepository;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<List<TransactionDTO>> getTransactions(Integer accountNumber) {
        List<Transaction> transactions = transactionsRepository.findAllByAccountNumber(accountNumber);
        List<TransactionDTO> list = new ArrayList<>();
        try {
            list.addAll(transactions.stream()
                    .map(this::convertToTransactionDTO)
                    .toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(list);
    }


    private TransactionDTO convertToTransactionDTO(Transaction transaction) {
        return objectMapper.convertValue(transaction, TransactionDTO.class);
    }

    public void save(TransactionDTO transaction, Integer accNum, BigDecimal amount) {
        Transaction transactionToSave = new Transaction();
        transactionToSave.setTransactionId(transaction.getTransactionId());
        transactionToSave.setAccountNumber(accNum);
        transactionToSave.setAmount(amount);
        transactionsRepository.save(transactionToSave);
    }
}
