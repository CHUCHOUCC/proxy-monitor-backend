package com.ucc.proxymonitor.service;

import com.ucc.proxymonitor.exception.BusinessRuleException;
import com.ucc.proxymonitor.exception.ResourceNotFoundException;
import com.ucc.proxymonitor.service.model.Order;
import com.ucc.proxymonitor.service.model.OrderStatus;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Real subject: orders microservice.
 * Operations: createOrder, getOrder, listOrders, cancelOrder.
 */
public class OrderService extends AbstractMicroservice {

    public static final String ID = "orders";

    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public OrderService(LatencySimulator latency) {
        super(ID, latency);
        registerOperation("createOrder", args -> createOrder(
                args.getString("customer"), args.getString("productId"), args.getPositiveInt("quantity")));
        registerOperation("getOrder", args -> findOrder(args.getString("orderId")));
        registerOperation("listOrders", args -> listOrders());
        registerOperation("cancelOrder", args -> cancelOrder(args.getString("orderId")));
    }

    private Order createOrder(String customer, String productId, int quantity) {
        String id = "ORD-" + sequence.incrementAndGet();
        Order order = new Order(id, customer, productId, quantity, OrderStatus.CREATED, Instant.now());
        orders.put(id, order);
        return order;
    }

    private Order findOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        return order;
    }

    private List<Order> listOrders() {
        return orders.values().stream()
                .sorted(Comparator.comparing(Order::createdAt).reversed())
                .limit(50)
                .toList();
    }

    private Order cancelOrder(String orderId) {
        Order order = findOrder(orderId);
        if (order.status() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Order " + orderId + " is already cancelled");
        }
        Order cancelled = order.withStatus(OrderStatus.CANCELLED);
        orders.put(orderId, cancelled);
        return cancelled;
    }
}
