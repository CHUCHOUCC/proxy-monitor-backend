package com.ucc.proxymonitor.exception;

/**
 * Thrown by the LoggingProxy when the wrapped service fails.
 * Carries the requestId so the client can find the call in the logs.
 */
public class ServiceExecutionException extends RuntimeException {

    private final String requestId;
    private final String serviceId;
    private final String operation;

    public ServiceExecutionException(String requestId, String serviceId, String operation, RuntimeException cause) {
        super(cause.getMessage(), cause);
        this.requestId = requestId;
        this.serviceId = serviceId;
        this.operation = operation;
    }

    public String getRequestId() { return requestId; }
    public String getServiceId() { return serviceId; }
    public String getOperation() { return operation; }
}
