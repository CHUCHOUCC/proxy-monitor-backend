package com.ucc.proxymonitor.service;

import com.ucc.proxymonitor.exception.UnknownOperationException;
import com.ucc.proxymonitor.proxy.MicroserviceProxy;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class of the real subjects. Each concrete service registers its
 * operations by name and this class dispatches execute(operation, params).
 */
public abstract class AbstractMicroservice implements MicroserviceProxy<Object> {

    private final String serviceId;
    private final LatencySimulator latency;
    private final Map<String, ServiceOperation> operations = new LinkedHashMap<>();

    protected AbstractMicroservice(String serviceId, LatencySimulator latency) {
        this.serviceId = serviceId;
        this.latency = latency;
    }

    protected final void registerOperation(String name, ServiceOperation operation) {
        operations.put(name, operation);
    }

    @Override
    public final Object execute(String operation, Object... params) {
        ServiceOperation handler = operations.get(operation);
        if (handler == null) {
            throw new UnknownOperationException(serviceId, operation, getOperations());
        }
        latency.pause();
        beforeOperation(operation);
        return handler.apply(OperationArgs.from(params));
    }

    /** Hook for subclasses that need to act before every operation. */
    protected void beforeOperation(String operation) {
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    public Set<String> getOperations() {
        return operations.keySet();
    }
}
