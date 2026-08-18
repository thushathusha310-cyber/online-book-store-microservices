package com.bookstore.payment.controller;

import com.bookstore.payment.dto.CreatePaymentRequest;
import com.bookstore.payment.dto.PaymentResponse;
import com.bookstore.payment.dto.UpdatePaymentStatusRequest;
import com.bookstore.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@SecurityRequirement(name = "ApiKeyAuth")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Create a pending payment for an order")
    ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(request));
    }

    @GetMapping
    @Operation(summary = "List all payments")
    List<PaymentResponse> findAll() {
        return paymentService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find a payment by ID")
    PaymentResponse findById(@PathVariable Long id) {
        return paymentService.findById(id);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "List payments for one order")
    List<PaymentResponse> findByOrder(@PathVariable Long orderId) {
        return paymentService.findByOrderId(orderId);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Mark a pending payment as SUCCESS or FAILED")
    PaymentResponse updateStatus(@PathVariable Long id,
                                 @Valid @RequestBody UpdatePaymentStatusRequest request) {
        return paymentService.updateStatus(id, request.status());
    }
}

