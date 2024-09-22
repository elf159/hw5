package com.example.accounts.exceptions;

public class LimitRateException extends RuntimeException{

    public LimitRateException(String messanger) {
        super(messanger);
    }
}
