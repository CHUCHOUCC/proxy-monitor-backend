package com.ucc.proxymonitor.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, String id) {
        super(resource + " '" + id + "' not found");
    }
}
