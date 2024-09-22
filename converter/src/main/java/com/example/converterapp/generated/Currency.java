package com.example.converterapp.generated;

import lombok.Getter;

@Getter
public enum Currency {

    RUB("RUB"),
    CNY("CNY"),
    EUR("EUR"),
    USD("USD"),
    GBP("GBP");

    private final String value;

    Currency(String value) {
        this.value = value;
    }
    public String toString() {
        return String.valueOf(value);
    }

    public static Currency fromValue(String input) {
        for (Currency b : Currency.values()) {
            if (b.value.equals(input)) {
                return b;
            }
        }
        return null;
    }
}