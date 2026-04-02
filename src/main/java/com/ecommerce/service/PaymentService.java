package com.ecommerce.service;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public String processPayment(double amount) {
        if (amount <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        return "SUCCESS";
    }
}