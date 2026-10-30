package com.ucc.proxymonitor.config;

import com.ucc.proxymonitor.logging.CallLogRepository;
import com.ucc.proxymonitor.logging.InMemoryCallLogRepository;
import com.ucc.proxymonitor.logging.StackTraceSummarizer;
import com.ucc.proxymonitor.proxy.LoggingProxy;
import com.ucc.proxymonitor.registry.ServiceRegistry;
import com.ucc.proxymonitor.service.FailureInjector;
import com.ucc.proxymonitor.service.InventoryService;
import com.ucc.proxymonitor.service.LatencySimulator;
import com.ucc.proxymonitor.service.OrderService;
import com.ucc.proxymonitor.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

/**
 * Wires every real service behind its own LoggingProxy.
 */
@Configuration
public class ProxyConfig {

    @Bean
    public Random random() {
        return new Random();
    }

    @Bean
    public CallLogRepository callLogRepository(@Value("${app.logs.max-size}") int maxSize) {
        return new InMemoryCallLogRepository(maxSize);
    }

    @Bean
    public StackTraceSummarizer stackTraceSummarizer() {
        return new StackTraceSummarizer(5);
    }

    @Bean
    public ServiceRegistry serviceRegistry(CallLogRepository repository,
                                           StackTraceSummarizer summarizer,
                                           Random random,
                                           @Value("${app.payments.failure-rate}") double failureRate) {
        ServiceRegistry registry = new ServiceRegistry();
        registry.register(new LoggingProxy<>(
                new InventoryService(new LatencySimulator(15, 90, random)), repository, summarizer));
        registry.register(new LoggingProxy<>(
                new OrderService(new LatencySimulator(25, 120, random)), repository, summarizer));
        registry.register(new LoggingProxy<>(
                new PaymentService(new LatencySimulator(40, 180, random), new FailureInjector(failureRate, random)),
                repository, summarizer));
        return registry;
    }
}
