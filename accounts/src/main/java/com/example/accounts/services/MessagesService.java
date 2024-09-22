package com.example.accounts.services;

import com.example.accounts.dto.MessageDTO;
import com.example.accounts.entities.Account;
import com.example.accounts.entities.Message;
import com.example.accounts.repositories.AccountsRepository;
import com.example.accounts.repositories.OutboxMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessagesService {
    private final OutboxMessageRepository outboxMessageRepository;
    private final AccountsRepository accountsRepository;
    private final OutBoxService outboxService;

    @Autowired
    public MessagesService(OutboxMessageRepository outboxMessageRepository, AccountsRepository accountsRepository,
                           OutBoxService outboxService) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.accountsRepository = accountsRepository;
        this.outboxService = outboxService;
    }

    @Scheduled(fixedDelayString = "5000")
    public void send() {
        List<Message> messages = outboxMessageRepository.findMessages();
        for (Message outboxMessage : messages) {
            MessageDTO message = null;
            Optional<Account> acc = accountsRepository.findAccountByAccountNumber(outboxMessage.getAccountNumber());
            if (acc.isPresent()) {
                Account account = acc.get();
                message = new MessageDTO(account.getCustomerId(), "Счет " + outboxMessage.getAccountNumber()
                        + ". " + "Операция: " + outboxMessage.getAmount()+ ". "
                        + "Баланс: " + outboxMessage.getBalance());
            }
            if (outboxService.outBoxSendMessage(message)) {
                outboxMessageRepository.delete(outboxMessage);
            }
        }
    }
}
