package com.ucc.proxymonitor.exception;

public class UnknownServiceException extends RuntimeException {

    public UnknownServiceException(String serviceId) {
        super("Service '" + serviceId + "' is not registered");
    }
}
