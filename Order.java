package com.example.tdm.model.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Sample domain model for generating order test data
 */
public record Order(
        @JsonProperty(required = true)
        String orderId,
        
        @JsonProperty(required = true)
        String customerId,
        
        @JsonProperty(required = true)
        LocalDateTime orderDate,
        
        @JsonProperty(required = true)
        OrderStatus status,
        
        @JsonProperty(required = true)
        List<OrderItem> items,
        
        @JsonProperty(required = true)
        BigDecimal totalAmount,
        
        String shippingAddress,
        
        String paymentMethod
) {
    public record OrderItem(
            String productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {}
    
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        REFUNDED
    }
}
