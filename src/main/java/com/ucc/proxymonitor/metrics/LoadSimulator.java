package com.ucc.proxymonitor.metrics;

import com.ucc.proxymonitor.exception.ServiceExecutionException;
import com.ucc.proxymonitor.registry.ServiceRegistry;
import com.ucc.proxymonitor.service.InventoryService;
import com.ucc.proxymonitor.service.OrderService;
import com.ucc.proxymonitor.service.PaymentService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * Generates random calls through the proxies to populate the logs.
 */
@Component
public class LoadSimulator {

    public static final int DEFAULT_CALLS = 50;

    private static final List<String> PRODUCTS = List.of("P-001", "P-002", "P-003", "P-004", "P-005");
    private static final List<String> CUSTOMERS = List.of("Ana", "Carlos", "Laura", "Andres", "Sofia");
    private static final List<String> METHODS = List.of("CARD", "PSE", "NEQUI");

    /** A call template: target service, operation and a generator of random arguments. */
    private record Scenario(String serviceId, String operation, Function<Random, Map<String, Object>> args) {
    }

    private final ServiceRegistry registry;
    private final Random random;
    private final List<Scenario> scenarios;

    public LoadSimulator(ServiceRegistry registry, Random random) {
        this.registry = registry;
        this.random = random;
        this.scenarios = List.of(
                new Scenario(InventoryService.ID, "listProducts", r -> Map.of()),
                new Scenario(InventoryService.ID, "getStock", r -> Map.of("productId", pick(PRODUCTS, r))),
                new Scenario(InventoryService.ID, "addStock",
                        r -> Map.of("productId", pick(PRODUCTS, r), "quantity", 1 + r.nextInt(10))),
                new Scenario(InventoryService.ID, "removeStock",
                        r -> Map.of("productId", pick(PRODUCTS, r), "quantity", 1 + r.nextInt(3))),
                new Scenario(OrderService.ID, "createOrder", r -> Map.of(
                        "customer", pick(CUSTOMERS, r), "productId", pick(PRODUCTS, r), "quantity", 1 + r.nextInt(5))),
                new Scenario(OrderService.ID, "listOrders", r -> Map.of()),
                new Scenario(PaymentService.ID, "processPayment", r -> Map.of(
                        "orderId", "ORD-" + (1 + r.nextInt(100)),
                        "amount", 50_000 + r.nextInt(2_000_000),
                        "method", pick(METHODS, r))));
    }

    public LoadSimulationResult simulate(int calls) {
        long start = System.currentTimeMillis();
        int success = 0;
        for (int i = 0; i < calls; i++) {
            Scenario scenario = pickScenario();
            try {
                registry.get(scenario.serviceId()).execute(scenario.operation(), scenario.args().apply(random));
                success++;
            } catch (ServiceExecutionException ex) {
                // Already registered by the LoggingProxy; the simulation keeps going.
            }
        }
        return new LoadSimulationResult(calls, success, calls - success, System.currentTimeMillis() - start);
    }

    /** Picks a random service first (uniform), then one of its scenarios. */
    private Scenario pickScenario() {
        List<String> serviceIds = registry.getServiceIds();
        String serviceId = pick(serviceIds, random);
        List<Scenario> candidates = scenarios.stream()
                .filter(s -> s.serviceId().equals(serviceId))
                .toList();
        return pick(candidates, random);
    }

    private static <T> T pick(List<T> values, Random random) {
        return values.get(random.nextInt(values.size()));
    }
}
