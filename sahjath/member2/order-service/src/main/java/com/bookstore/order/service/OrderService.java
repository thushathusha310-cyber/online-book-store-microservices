package com.bookstore.order.service;

import com.bookstore.order.dto.CreateOrderItemRequest;
import com.bookstore.order.dto.CreateOrderRequest;
import com.bookstore.order.dto.OrderItemResponse;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.entity.OrderEntity;
import com.bookstore.order.entity.OrderItem;
import com.bookstore.order.entity.OrderStatus;
import com.bookstore.order.exception.InvalidOrderStateException;
import com.bookstore.order.exception.ResourceNotFoundException;
import com.bookstore.order.repository.OrderRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class OrderService {
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = buildTransitions();

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderResponse create(CreateOrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setUserId(request.userId());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderItemRequest requestItem : request.items()) {
            OrderItem item = new OrderItem();
            item.setBookId(requestItem.bookId());
            item.setBookTitle(requestItem.bookTitle().trim());
            item.setQuantity(requestItem.quantity());
            item.setUnitPrice(requestItem.unitPrice());
            BigDecimal subtotal = requestItem.unitPrice().multiply(BigDecimal.valueOf(requestItem.quantity()));
            item.setSubtotal(subtotal);
            order.addItem(item);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    public OrderResponse updateStatus(Long id, OrderStatus nextStatus) {
        OrderEntity order = findEntity(id);
        if (order.getStatus() == nextStatus) {
            return toResponse(order);
        }

        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        if (!allowed.contains(nextStatus)) {
            throw new InvalidOrderStateException(
                    "Order status cannot change from " + order.getStatus() + " to " + nextStatus);
        }

        order.setStatus(nextStatus);
        return toResponse(orderRepository.save(order));
    }

    public void delete(Long id) {
        OrderEntity order = findEntity(id);
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Only PENDING or CANCELLED orders can be deleted");
        }
        orderRepository.delete(order);
    }

    private OrderEntity findEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + id + " was not found"));
    }

    private OrderResponse toResponse(OrderEntity order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(), item.getBookId(), item.getBookTitle(), item.getQuantity(),
                        item.getUnitPrice(), item.getSubtotal()))
                .toList();

        return new OrderResponse(
                order.getId(), order.getUserId(), itemResponses, order.getTotalAmount(),
                order.getStatus(), order.getCreatedAt(), order.getUpdatedAt());
    }

    private static Map<OrderStatus, Set<OrderStatus>> buildTransitions() {
        Map<OrderStatus, Set<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
        transitions.put(OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.PAID, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.PAID, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED));
        transitions.put(OrderStatus.DELIVERED, Set.of());
        transitions.put(OrderStatus.CANCELLED, Set.of());
        return Map.copyOf(transitions);
    }
}

