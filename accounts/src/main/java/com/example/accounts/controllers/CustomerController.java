package com.example.accounts.controllers;


import com.example.accounts.dto.*;
import com.example.accounts.exceptions.LimitRateException;
import com.example.accounts.services.BucketService;
import com.example.accounts.services.CustomerService;
import io.github.bucket4j.Bucket;
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
    private final BucketService bucketService;
    @Autowired
    public CustomerController(CustomerService customerService, BucketService bucketService) {
        this.customerService = customerService;
        this.bucketService = bucketService;

    }
    @PostMapping("/customers")
    ResponseEntity<CustomerCreationResponse> createCustomer(@RequestBody CustomerDTO customerDTO) {
        return customerService.createCustomer(customerDTO);
    }

    @GetMapping("/customers/{customerId}/balance")
    public ResponseEntity<AllBalanceCustomerResponse> showBalance(@PathVariable(value = "customerId") Integer id,
                                                                  @RequestParam(value = "currency") String currency) {
        Bucket bucket = bucketService.get(id);
        if (!bucket.tryConsume(1)) {
            throw new LimitRateException("Limit on taking rate");
        }
        return customerService.showBalance(id, currency);
    }

    @Transactional
    @PostMapping("/transfers")
    public ResponseEntity<?> transferMoney(@RequestBody TransferDTO transferDTO) {
        return customerService.transfer(transferDTO);
    }
}
