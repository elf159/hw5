package com.example.accounts.controllers;


import com.example.accounts.dto.*;
import com.example.accounts.services.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping
public class CustomerController {
    CustomerService customerService;
    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }
    @PostMapping("/customers")
    ResponseEntity<CustomerCreationResponse> createCustomer(@RequestBody CustomerDTO customerDTO) {
        return customerService.createCustomer(customerDTO);
    }

    @GetMapping("/customers/{customerId}/balance")
    public ResponseEntity<AllBalanceCustomerResponse> showBalance(@PathVariable(value = "customerId") Integer id,
                                                                  @RequestParam(value = "currency") String currency) {
        return customerService.showBalance(id, currency);
    }

    @Transactional
    @PostMapping("/transfers")
    public ResponseEntity<?> transferMoney(@RequestBody TransferDTO transferDTO) {
        return customerService.transfer(transferDTO);
    }
}
