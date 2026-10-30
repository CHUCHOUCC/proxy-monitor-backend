package com.ucc.proxymonitor.web;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, String error, String message,
                            String requestId, String serviceId, String operation) {
}
