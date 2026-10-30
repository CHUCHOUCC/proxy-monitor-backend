package com.ucc.proxymonitor.service;

/**
 * One operation exposed by a microservice.
 */
@FunctionalInterface
public interface ServiceOperation {

    Object apply(OperationArgs args);
}
