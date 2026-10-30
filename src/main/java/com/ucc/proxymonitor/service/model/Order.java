package com.ucc.proxymonitor.service.model;

import java.time.Instant;

public record Order(String id, String customer, String productId, int quantity,
                    OrderStatus status, Instant createdAt) {

    public Order withStatus(OrderStatus newStatus) {
        return new Order(id, customer, productId, quantity, newStatus, createdAt);
    }
}
