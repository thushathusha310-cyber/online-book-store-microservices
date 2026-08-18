package com.bookstore.payment.dto;

import com.bookstore.payment.entity.PaymentMethod;
import com.bookstore.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long orderId,
        Long userId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String transactionReference,
        Instant createdAt,
        Instant updatedAt
) {
}

