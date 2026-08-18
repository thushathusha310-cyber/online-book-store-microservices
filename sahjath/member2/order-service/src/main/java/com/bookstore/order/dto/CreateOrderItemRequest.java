package com.bookstore.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateOrderItemRequest(
        @NotNull(message = "bookId is required")
        Long bookId,

        @NotBlank(message = "bookTitle is required")
        @Size(max = 200, message = "bookTitle cannot exceed 200 characters")
        String bookTitle,

        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be at least 1")
        Integer quantity,

        @NotNull(message = "unitPrice is required")
        @DecimalMin(value = "0.01", message = "unitPrice must be greater than zero")
        BigDecimal unitPrice
) {
}

