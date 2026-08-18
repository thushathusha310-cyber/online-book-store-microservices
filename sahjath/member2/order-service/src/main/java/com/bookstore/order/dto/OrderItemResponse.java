package com.bookstore.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}

