package com.ucc.proxymonitor.web;

import com.ucc.proxymonitor.registry.ServiceRegistry;
import com.ucc.proxymonitor.service.InventoryService;
import com.ucc.proxymonitor.service.OrderService;
import com.ucc.proxymonitor.service.PaymentService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Client of the Proxy pattern: every request goes through the LoggingProxy
 * registered for the service. The body is a JSON object with named arguments.
 */
@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceRegistry registry;

    public ServiceController(ServiceRegistry registry) {
        this.registry = registry;
    }

    @PostMapping("/inventory/{operation}")
    public Object inventory(@PathVariable String operation,
                            @RequestBody(required = false) Map<String, Object> params) {
        return invoke(InventoryService.ID, operation, params);
    }

    @PostMapping("/orders/{operation}")
    public Object orders(@PathVariable String operation,
                         @RequestBody(required = false) Map<String, Object> params) {
        return invoke(OrderService.ID, operation, params);
    }

    @PostMapping("/payments/{operation}")
    public Object payments(@PathVariable String operation,
                           @RequestBody(required = false) Map<String, Object> params) {
        return invoke(PaymentService.ID, operation, params);
    }

    private Object invoke(String serviceId, String operation, Map<String, Object> params) {
        return registry.get(serviceId).execute(operation, params == null ? Map.of() : params);
    }
}
