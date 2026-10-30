package com.ucc.proxymonitor.proxy;

import com.ucc.proxymonitor.exception.ServiceExecutionException;
import com.ucc.proxymonitor.logging.CallLog;
import com.ucc.proxymonitor.logging.CallStatus;
import com.ucc.proxymonitor.logging.InMemoryCallLogRepository;
import com.ucc.proxymonitor.logging.LogFilter;
import com.ucc.proxymonitor.logging.StackTraceSummarizer;
import com.ucc.proxymonitor.service.FailureInjector;
import com.ucc.proxymonitor.service.InventoryService;
import com.ucc.proxymonitor.service.LatencySimulator;
import com.ucc.proxymonitor.service.PaymentService;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LoggingProxyTest {

    private final InMemoryCallLogRepository repository = new InMemoryCallLogRepository(100);
    private final StackTraceSummarizer summarizer = new StackTraceSummarizer(5);

    @Test
    void successfulCallIsLoggedWithAllFields() {
        MicroserviceProxy<Object> proxy =
                new LoggingProxy<>(new InventoryService(LatencySimulator.none()), repository, summarizer);

        proxy.execute("getStock", Map.of("productId", "P-001"));

        CallLog log = repository.findBy(LogFilter.all()).get(0);
        assertEquals("inventory", log.getServiceId());
        assertEquals("getStock", log.getOperation());
        assertEquals(CallStatus.SUCCESS, log.getStatus());
        assertNotNull(log.getTimestamp());
        assertNotNull(log.getResponse());
        assertTrue(log.getDurationMs() >= 0);
        assertDoesNotThrow(() -> UUID.fromString(log.getRequestId()));
    }

    @Test
    void failedCallIsLoggedWithSummarizedStackTrace() {
        MicroserviceProxy<Object> proxy =
                new LoggingProxy<>(new InventoryService(LatencySimulator.none()), repository, summarizer);

        ServiceExecutionException ex = assertThrows(ServiceExecutionException.class,
                () -> proxy.execute("getStock", Map.of("productId", "NOPE")));

        CallLog log = repository.findBy(LogFilter.all()).get(0);
        assertEquals(CallStatus.ERROR, log.getStatus());
        assertEquals(ex.getRequestId(), log.getRequestId());
        assertFalse(log.getStackTrace().isEmpty());
        assertTrue(log.getStackTrace().size() <= 7);
    }

    @Test
    void paymentServiceFailsAboutTenPercent() {
        MicroserviceProxy<Object> proxy = new LoggingProxy<>(
                new PaymentService(LatencySimulator.none(), new FailureInjector(0.10, new Random(42))),
                repository, summarizer);

        int errors = 0;
        for (int i = 0; i < 1000; i++) {
            try {
                proxy.execute("processPayment", Map.of("orderId", "ORD-1", "amount", 1000));
            } catch (ServiceExecutionException e) {
                errors++;
            }
        }
        assertTrue(errors > 60 && errors < 140, "errors=" + errors);
    }
}
