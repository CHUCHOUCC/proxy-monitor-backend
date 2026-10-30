package com.ucc.proxymonitor.service.model;

/** Immutable product snapshot (safe to keep inside logs). */
public record Product(String id, String name, double price, int stock) {

    public Product withStock(int newStock) {
        return new Product(id, name, price, newStock);
    }
}
