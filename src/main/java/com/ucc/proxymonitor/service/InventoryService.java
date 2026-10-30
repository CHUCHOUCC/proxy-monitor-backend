package com.ucc.proxymonitor.service;

import com.ucc.proxymonitor.exception.BusinessRuleException;
import com.ucc.proxymonitor.exception.ResourceNotFoundException;
import com.ucc.proxymonitor.service.model.Product;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real subject: inventory microservice.
 * Operations: listProducts, getStock, addStock, removeStock.
 */
public class InventoryService extends AbstractMicroservice {

    public static final String ID = "inventory";

    private final Map<String, Product> products = new ConcurrentHashMap<>();

    public InventoryService(LatencySimulator latency) {
        super(ID, latency);
        seedProducts();
        registerOperation("listProducts", args -> listProducts());
        registerOperation("getStock", args -> findProduct(args.getString("productId")));
        registerOperation("addStock", args -> addStock(args.getString("productId"), args.getPositiveInt("quantity")));
        registerOperation("removeStock", args -> removeStock(args.getString("productId"), args.getPositiveInt("quantity")));
    }

    private void seedProducts() {
        save(new Product("P-001", "Laptop", 3_500_000, 120));
        save(new Product("P-002", "Mouse", 80_000, 300));
        save(new Product("P-003", "Keyboard", 150_000, 200));
        save(new Product("P-004", "Monitor", 900_000, 80));
        save(new Product("P-005", "Headset", 250_000, 150));
    }

    private void save(Product product) {
        products.put(product.id(), product);
    }

    private List<Product> listProducts() {
        return products.values().stream()
                .sorted((a, b) -> a.id().compareTo(b.id()))
                .toList();
    }

    private Product findProduct(String productId) {
        Product product = products.get(productId);
        if (product == null) {
            throw new ResourceNotFoundException("Product", productId);
        }
        return product;
    }

    private Product addStock(String productId, int quantity) {
        findProduct(productId);
        return products.computeIfPresent(productId, (id, p) -> p.withStock(p.stock() + quantity));
    }

    private Product removeStock(String productId, int quantity) {
        findProduct(productId);
        return products.computeIfPresent(productId, (id, p) -> {
            if (p.stock() < quantity) {
                throw new BusinessRuleException("Insufficient stock for " + id + ": available "
                        + p.stock() + ", requested " + quantity);
            }
            return p.withStock(p.stock() - quantity);
        });
    }
}
