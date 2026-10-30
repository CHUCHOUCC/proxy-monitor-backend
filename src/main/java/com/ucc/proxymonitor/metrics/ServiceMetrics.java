package com.ucc.proxymonitor.metrics;

public record ServiceMetrics(String serviceId,
                             long totalCalls,
                             long successCount,
                             long errorCount,
                             double successRate,
                             double errorRate,
                             double avgResponseTimeMs) {
}
