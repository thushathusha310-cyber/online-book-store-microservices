package com.bookstore.payment.service;

import com.bookstore.payment.client.OrderClient;
import com.bookstore.payment.client.OrderSummary;
import com.bookstore.payment.dto.CreatePaymentRequest;
import com.bookstore.payment.dto.PaymentResponse;
import com.bookstore.payment.entity.Payment;
import com.bookstore.payment.entity.PaymentStatus;
import com.bookstore.payment.exception.InvalidPaymentException;
import com.bookstore.payment.exception.ResourceNotFoundException;
import com.bookstore.payment.repository.PaymentRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {
    private static final Set<String> PAYABLE_ORDER_STATES = Set.of("PENDING", "CONFIRMED");

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    public PaymentService(PaymentRepository paymentRepository, OrderClient orderClient) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
    }

    public PaymentResponse create(CreatePaymentRequest request) {
        OrderSummary order = orderClient.findById(request.orderId());
        if (order == null) {
            throw new ResourceNotFoundException("Order " + request.orderId() + " was not found");
        }
        if (!PAYABLE_ORDER_STATES.contains(order.status())) {
            throw new InvalidPaymentException("Order in status " + order.status() + " cannot be paid");
        }
        if (order.totalAmount().compareTo(request.amount()) != 0) {
            throw new InvalidPaymentException(
                    "Payment amount must equal order total " + order.totalAmount());
        }

        boolean alreadyPaid = paymentRepository.findByOrderIdOrderByCreatedAtDesc(request.orderId())
                .stream().anyMatch(payment -> payment.getStatus() == PaymentStatus.SUCCESS);
        if (alreadyPaid) {
            throw new InvalidPaymentException("This order already has a successful payment");
        }

        Payment payment = new Payment();
        payment.setOrderId(order.id());
        payment.setUserId(order.userId());
        payment.setAmount(request.amount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionReference("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());

        return toResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findAll() {
        return paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByOrderId(Long orderId) {
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream().map(this::toResponse).toList();
    }

    public PaymentResponse updateStatus(Long id, PaymentStatus nextStatus) {
        Payment payment = findEntity(id);
        if (payment.getStatus() == nextStatus) {
            return toResponse(payment);
        }
        if (payment.getStatus() != PaymentStatus.PENDING || nextStatus == PaymentStatus.PENDING) {
            throw new InvalidPaymentException(
                    "Payment status cannot change from " + payment.getStatus() + " to " + nextStatus);
        }

        if (nextStatus == PaymentStatus.SUCCESS) {
            orderClient.markAsPaid(payment.getOrderId());
        }

        payment.setStatus(nextStatus);
        return toResponse(paymentRepository.save(payment));
    }

    private Payment findEntity(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment " + id + " was not found"));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(), payment.getOrderId(), payment.getUserId(), payment.getAmount(),
                payment.getPaymentMethod(), payment.getStatus(), payment.getTransactionReference(),
                payment.getCreatedAt(), payment.getUpdatedAt());
    }
}

