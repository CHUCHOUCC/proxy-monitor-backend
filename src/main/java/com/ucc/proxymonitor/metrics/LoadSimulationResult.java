package com.ucc.proxymonitor.metrics;

public record LoadSimulationResult(int requestedCalls, int successfulCalls, int failedCalls, long durationMs) {
}
