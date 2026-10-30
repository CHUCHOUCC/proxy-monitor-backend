package com.ucc.proxymonitor.metrics;

import java.time.Instant;
import java.util.List;

public record MetricsSummary(Instant generatedAt, long totalCalls, List<ServiceMetrics> services) {
}
