package com.bookstore.payment.client;

import java.math.BigDecimal;

public record OrderSummary(
        Long id,
        Long userId,
        BigDecimal totalAmount,
        String status
) {
}

