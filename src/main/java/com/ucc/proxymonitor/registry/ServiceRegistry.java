package com.ucc.proxymonitor.registry;

import com.ucc.proxymonitor.exception.UnknownServiceException;
import com.ucc.proxymonitor.proxy.MicroserviceProxy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps the proxies by service id. The rest of the application only sees
 * MicroserviceProxy references: it never touches the real services directly.
 */
public class ServiceRegistry {

    private final Map<String, MicroserviceProxy<Object>> services = new LinkedHashMap<>();

    public void register(MicroserviceProxy<Object> service) {
        services.put(service.getServiceId(), service);
    }

    public MicroserviceProxy<Object> get(String serviceId) {
        MicroserviceProxy<Object> service = services.get(serviceId);
        if (service == null) {
            throw new UnknownServiceException(serviceId);
        }
        return service;
    }

    public List<String> getServiceIds() {
        return List.copyOf(services.keySet());
    }
}
