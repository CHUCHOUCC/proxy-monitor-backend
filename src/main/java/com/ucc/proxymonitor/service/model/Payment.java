package com.ucc.proxymonitor.service.model;

import java.time.Instant;

public record Payment(String id, String orderId, double amount, String method,
                      PaymentStatus status, Instant createdAt) {

    public Payment withStatus(PaymentStatus newStatus) {
        return new Payment(id, orderId, amount, method, newStatus, createdAt);
    }
}
