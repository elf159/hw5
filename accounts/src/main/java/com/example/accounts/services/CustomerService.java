package com.example.accounts.services;

import com.example.accounts.config.AccountsConfigEnv;
import com.example.accounts.dto.*;
import com.example.accounts.entities.Account;
import com.example.accounts.entities.Customer;
import com.example.accounts.entities.Message;
import com.example.accounts.exceptions.GRPCProcessException;
import com.example.accounts.repositories.AccountsRepository;
import com.example.accounts.repositories.CustomerRepository;
import com.example.accounts.repositories.OutboxMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final AccountsRepository accountsRepository;
    private final AccountsConfigEnv accountsConfigEnv;
    private final RestTemplate restTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConverterControllerGrpcClient client;
    private final OutboxMessageRepository outboxMessageRepository;


    @Autowired
    public CustomerService(CustomerRepository customerRepository, AccountsRepository accountsRepository,
                           AccountsConfigEnv accountsConfigEnv,
                           RestTemplate restTemplate, SimpMessagingTemplate simpMessagingTemplate,
                           ConverterControllerGrpcClient client, OutboxMessageRepository outboxMessageRepository) {
        this.customerRepository = customerRepository;
        this.accountsRepository = accountsRepository;
        this.accountsConfigEnv = accountsConfigEnv;
        this.restTemplate = restTemplate;
        this.outboxMessageRepository = outboxMessageRepository;
        this.restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.client = client;

    }
    private static final Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    public ResponseEntity<CustomerCreationResponse> createCustomer(CustomerDTO customerDTO) {
        if (checkCustomer(customerDTO)) {
            return ResponseEntity.badRequest().build();
        }
        if (!LocalDate.parse(customerDTO.getBirthDay().toString()).isBefore(LocalDate.now()) ||
                Period.between(customerDTO.getBirthDay(),
                LocalDate.now()).getYears() < 14 ||
                Period.between(customerDTO.getBirthDay(),
                LocalDate.now()).getYears() > 120) {
            return ResponseEntity.badRequest().build();
        }
        Customer preCustomer = new Customer();
        preCustomer.setFirstName(customerDTO.getFirstName());
        preCustomer.setLastName(customerDTO.getLastName());
        preCustomer.setBirthday(customerDTO.getBirthDay());
        try {
            preCustomer = customerRepository.save(preCustomer);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
        if (preCustomer.getId() == 0) {
            return ResponseEntity.internalServerError().build();
        }
        CustomerCreationResponse customerCreationResponse = new CustomerCreationResponse();
        customerCreationResponse.setCustomerId(preCustomer.getId());
        return ResponseEntity.status(HttpStatus.OK).body(customerCreationResponse);
    }
    public boolean checkCustomer(CustomerDTO customerDTO) {
        return customerDTO.getFirstName() == null || customerDTO.getFirstName().isEmpty() || customerDTO.getLastName() == null
                || customerDTO.getLastName().isEmpty() || customerDTO.getBirthDay() == null || customerDTO.getBirthDay().toString().isEmpty()
                || !pattern.matcher(customerDTO.getBirthDay().toString()).matches();
    }

    public ResponseEntity<AllBalanceCustomerResponse> showBalance(Integer id, String currency) {
        if (checkInput(id, currency)) {
            return ResponseEntity.badRequest().build();
        }
        List<Account> accounts = findCustomerAccounts(id);
        BigDecimal total = BigDecimal.ZERO;
        for (Account account : accounts) {
            BigDecimal currentBalance = convert(account.getCurrency(), currency, account.getBalance());
            total = total.add(currentBalance);
        }
        AllBalanceCustomerResponse response = new AllBalanceCustomerResponse();
        response.setCurrency(currency);
        response.setBalance(total);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private boolean checkInput(Integer id, String currency) {
        return String.valueOf(id).isEmpty() && currency == null && currency.isEmpty() &&
                !String.valueOf(id).matches("\\d+") && !accountsConfigEnv.getAllowedCurrencies().contains(currency);
    }

    private List<Account> findCustomerAccounts(Integer id) {
        return accountsRepository.findAccountByCustomerId(id);
    }
    private BigDecimal convert(String fromCurrency, String toCurrency, BigDecimal amount) {
        converter.ConvertResponse response;
        try {
            response = client.convert(fromCurrency, toCurrency, amount);
        } catch (Exception e) {
            throw new GRPCProcessException("Conversion error");
        }
        if (response == null) {
            throw new GRPCProcessException("NULL ANSWER :(");
        }
        return BigDecimal.valueOf(response.getAmount());
    }

    public ResponseEntity<?> transfer(TransferDTO transferDTO) {
        if (!checkDTO(transferDTO)) {
            return ResponseEntity.badRequest().build();
        }
        int senderFromDTO = parseAccountNumber(String.valueOf(transferDTO.getSenderAccount()));
        int receiverFromDTO = parseAccountNumber(String.valueOf(transferDTO.getReceiverAccount()));
        BigDecimal amount = transferDTO.getAmountInSenderCurrency();
        Optional<Account> optSender = findAccountByAccountNumber(senderFromDTO);
        Optional<Account> optReceiver = findAccountByAccountNumber(receiverFromDTO);
        if (optSender.isEmpty() || optReceiver.isEmpty()) {
            return ResponseEntity.internalServerError().build();
        }
        Account sender = optSender.get();
        Account receiver = optReceiver.get();
        transferMoney(sender, receiver, amount);
        return ResponseEntity.ok().build();
    }

    private boolean checkDTO(TransferDTO transferDTO) {
        return transferDTO != null &&
                transferDTO.getSenderAccount() != null &&
                transferDTO.getReceiverAccount() != null &&
                transferDTO.getAmountInSenderCurrency() != null &&
                !transferDTO.getSenderAccount().toString().isEmpty() &&
                !transferDTO.getReceiverAccount().toString().isEmpty() &&
                checkNumber(String.valueOf(transferDTO.getSenderAccount())) &&
                checkNumber(String.valueOf(transferDTO.getReceiverAccount()));
    }

    private int parseAccountNumber(String accountNumber) {
        try {
            return Integer.parseInt(accountNumber);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Incorrect account number: " + accountNumber);
        }
    }

    private Optional<Account> findAccountByAccountNumber(int accountNumber) {
        return accountsRepository.findAccountByAccountNumber(accountNumber);
    }

    private void transferMoney(Account sender, Account receiver, BigDecimal amount) {
        BigDecimal convAmount = convert(sender.getCurrency(), receiver.getCurrency(), amount);
        receiver.setBalance(receiver.getBalance().add(convAmount));
        sender.setBalance(sender.getBalance().subtract(amount));
        accountsRepository.save(sender);
        accountsRepository.save(receiver);
        AccountSocketDTO senderSocketDto = new AccountSocketDTO();
        senderSocketDto.setAccountNumber(sender.getAccountNumber());
        senderSocketDto.setCurrency(sender.getCurrency());
        senderSocketDto.setBalance(sender.getBalance());

        AccountSocketDTO receiverSocketDto = new AccountSocketDTO();
        receiverSocketDto.setAccountNumber(receiver.getAccountNumber());
        receiverSocketDto.setBalance(receiver.getBalance());
        receiverSocketDto.setCurrency(receiver.getCurrency());

        simpMessagingTemplate.convertAndSend("/topic/accounts", senderSocketDto);
        simpMessagingTemplate.convertAndSend("/topic/accounts", receiverSocketDto);

        Message messageSender = new Message();
        messageSender.setAccountNumber(sender.getAccountNumber());
        messageSender.setAmount(amount);
        messageSender.setBalance(sender.getBalance());
        outboxMessageRepository.save(messageSender);

        Message messageReceiver = new Message();
        messageReceiver.setAccountNumber(receiver.getAccountNumber());
        messageReceiver.setAmount(amount);
        messageReceiver.setBalance(receiver.getBalance());
        outboxMessageRepository.save(messageReceiver);

    }
    private static boolean checkNumber(String str) {
        return str.matches("[1-9][0-9]*");
    }
}
