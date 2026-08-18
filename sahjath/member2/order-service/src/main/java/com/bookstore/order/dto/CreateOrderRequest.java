package com.bookstore.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotEmpty(message = "at least one order item is required")
        List<@Valid CreateOrderItemRequest> items
) {
}

