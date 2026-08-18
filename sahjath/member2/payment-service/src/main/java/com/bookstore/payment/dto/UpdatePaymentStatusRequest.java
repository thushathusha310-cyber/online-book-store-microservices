package com.bookstore.payment.dto;

import com.bookstore.payment.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePaymentStatusRequest(
        @NotNull(message = "status is required")
        PaymentStatus status
) {
}

