package com.ucc.proxymonitor.web;

import com.ucc.proxymonitor.logging.CallLog;
import com.ucc.proxymonitor.logging.LogFilter;
import com.ucc.proxymonitor.metrics.LoadSimulationResult;
import com.ucc.proxymonitor.metrics.LoadSimulator;
import com.ucc.proxymonitor.metrics.MetricsService;
import com.ucc.proxymonitor.metrics.MetricsSummary;
import com.ucc.proxymonitor.metrics.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;
    private final LoadSimulator loadSimulator;

    public MetricsController(MetricsService metricsService, LoadSimulator loadSimulator) {
        this.metricsService = metricsService;
        this.loadSimulator = loadSimulator;
    }

    @GetMapping("/summary")
    public MetricsSummary summary() {
        return metricsService.getSummary();
    }

    @GetMapping("/logs")
    public PageResponse<CallLog> logs(@RequestParam(required = false) String service,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) String from,
                                      @RequestParam(required = false) String to,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return metricsService.getLogs(LogFilter.fromQuery(service, status, from, to), page, size);
    }

    @PostMapping("/simulate-load")
    public LoadSimulationResult simulateLoad() {
        return loadSimulator.simulate(LoadSimulator.DEFAULT_CALLS);
    }
}
