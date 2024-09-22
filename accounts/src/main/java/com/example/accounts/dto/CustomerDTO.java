package com.example.accounts.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerDTO {

    private String firstName;

    private String lastName;

    private LocalDate birthDay;


}
