package com.ucc.proxymonitor.exception;

import java.util.Set;

public class UnknownOperationException extends RuntimeException {

    public UnknownOperationException(String serviceId, String operation, Set<String> available) {
        super("Operation '" + operation + "' not supported by " + serviceId + ". Available: " + available);
    }
}
