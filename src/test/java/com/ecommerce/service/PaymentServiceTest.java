package com.ecommerce.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    @Test
    void processPayment_shouldReturnSuccess() {

        PaymentService service = new PaymentService();

        String result = service.processPayment(100);

        assertEquals("SUCCESS", result);
    }
}