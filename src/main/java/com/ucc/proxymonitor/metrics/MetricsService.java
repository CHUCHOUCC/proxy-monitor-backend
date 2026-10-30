package com.ucc.proxymonitor.metrics;

import com.ucc.proxymonitor.exception.InvalidArgumentsException;
import com.ucc.proxymonitor.logging.CallLog;
import com.ucc.proxymonitor.logging.CallLogRepository;
import com.ucc.proxymonitor.logging.LogFilter;
import com.ucc.proxymonitor.registry.ServiceRegistry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Computes metrics from the logs collected by the LoggingProxy.
 */
@Service
public class MetricsService {

    private static final int MAX_PAGE_SIZE = 200;

    private final CallLogRepository repository;
    private final ServiceRegistry registry;

    public MetricsService(CallLogRepository repository, ServiceRegistry registry) {
        this.repository = repository;
        this.registry = registry;
    }

    public MetricsSummary getSummary() {
        List<ServiceMetrics> services = registry.getServiceIds().stream()
                .map(this::computeMetrics)
                .toList();
        long total = services.stream().mapToLong(ServiceMetrics::totalCalls).sum();
        return new MetricsSummary(Instant.now(), total, services);
    }

    public PageResponse<CallLog> getLogs(LogFilter filter, int page, int size) {
        if (page < 0) {
            throw new InvalidArgumentsException("'page' must be >= 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidArgumentsException("'size' must be between 1 and " + MAX_PAGE_SIZE);
        }
        List<CallLog> matches = repository.findBy(filter);
        int fromIndex = Math.min(page * size, matches.size());
        int toIndex = Math.min(fromIndex + size, matches.size());
        int totalPages = (int) Math.ceil((double) matches.size() / size);
        return new PageResponse<>(matches.subList(fromIndex, toIndex), page, size, matches.size(), totalPages);
    }

    private ServiceMetrics computeMetrics(String serviceId) {
        List<CallLog> logs = repository.findBy(LogFilter.forService(serviceId));
        long total = logs.size();
        long success = logs.stream().filter(CallLog::isSuccess).count();
        long errors = total - success;
        double avg = logs.stream().mapToLong(CallLog::getDurationMs).average().orElse(0);
        return new ServiceMetrics(serviceId, total, success, errors,
                percentage(success, total), percentage(errors, total), round(avg));
    }

    private double percentage(long part, long total) {
        return total == 0 ? 0 : round(part * 100.0 / total);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
