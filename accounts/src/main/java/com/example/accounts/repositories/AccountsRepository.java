package com.example.accounts.repositories;

import com.example.accounts.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountsRepository extends JpaRepository<Account, Long> {
    List<Account> findAccountByCustomerId(Integer customer_id);
    Optional<Account> findAccountByAccountNumber(Integer accountNumber);
}
