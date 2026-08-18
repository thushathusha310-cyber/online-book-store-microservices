package com.bookstore.payment.client;

import com.bookstore.payment.exception.DownstreamServiceException;
import com.bookstore.payment.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class OrderClient {
    private final RestClient restClient;

    public OrderClient(RestClient.Builder builder,
                       @Value("${order-service.url}") String orderServiceUrl,
                       @Value("${order-service.api-key}") String orderServiceApiKey) {
        this.restClient = builder
                .baseUrl(orderServiceUrl)
                .defaultHeader("X-API-KEY", orderServiceApiKey)
                .build();
    }

    public OrderSummary findById(Long orderId) {
        try {
            return restClient.get()
                    .uri("/api/orders/{id}", orderId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new ResourceNotFoundException("Order " + orderId + " was not found");
                    })
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new DownstreamServiceException("Order Service rejected the request");
                    })
                    .body(OrderSummary.class);
        } catch (ResourceNotFoundException | DownstreamServiceException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new DownstreamServiceException("Order Service is unavailable");
        }
    }

    public void markAsPaid(Long orderId) {
        try {
            restClient.patch()
                    .uri("/api/orders/{id}/status", orderId)
                    .body(Map.of("status", "PAID"))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new DownstreamServiceException("Order Service could not mark order as PAID");
                    })
                    .toBodilessEntity();
        } catch (DownstreamServiceException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new DownstreamServiceException("Order Service is unavailable");
        }
    }
}
