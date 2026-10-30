package com.ucc.proxymonitor.proxy;

/**
 * Subject of the Proxy pattern.
 * Common interface shared by the real microservices and the LoggingProxy,
 * so the client never knows whether it is talking to the real object or to the proxy.
 *
 * @param <T> type of the result returned by an operation
 */
public interface MicroserviceProxy<T> {

    T execute(String operation, Object... params);

    String getServiceId();
}
