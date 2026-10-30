package com.ucc.proxymonitor.proxy;

import com.ucc.proxymonitor.exception.ServiceExecutionException;
import com.ucc.proxymonitor.logging.CallLog;
import com.ucc.proxymonitor.logging.CallLogRepository;
import com.ucc.proxymonitor.logging.StackTraceSummarizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/**
 * Proxy of the Proxy pattern.
 * Wraps ANY microservice and intercepts every call:
 *  - before: registers the start of the call
 *  - after: registers duration and result
 *  - on failure: registers the error with a summarized stack trace
 */
public class LoggingProxy<T> implements MicroserviceProxy<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingProxy.class);

    private final MicroserviceProxy<T> target;
    private final CallLogRepository repository;
    private final StackTraceSummarizer stackTraceSummarizer;

    public LoggingProxy(MicroserviceProxy<T> target,
                        CallLogRepository repository,
                        StackTraceSummarizer stackTraceSummarizer) {
        this.target = target;
        this.repository = repository;
        this.stackTraceSummarizer = stackTraceSummarizer;
    }

    @Override
    public T execute(String operation, Object... params) {
        String requestId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();
        long start = System.nanoTime();

        LOGGER.info("[{}] START {}.{} params={}", requestId, getServiceId(), operation, Arrays.toString(params));

        try {
            T result = target.execute(operation, params);
            long durationMs = elapsedMs(start);
            repository.save(CallLog.success(requestId, getServiceId(), operation, durationMs, timestamp, params, result));
            LOGGER.info("[{}] SUCCESS {}.{} in {} ms", requestId, getServiceId(), operation, durationMs);
            return result;
        } catch (RuntimeException ex) {
            long durationMs = elapsedMs(start);
            repository.save(CallLog.error(requestId, getServiceId(), operation, durationMs, timestamp, params,
                    ex, stackTraceSummarizer.summarize(ex)));
            LOGGER.warn("[{}] ERROR {}.{} in {} ms -> {}", requestId, getServiceId(), operation, durationMs, ex.getMessage());
            throw new ServiceExecutionException(requestId, getServiceId(), operation, ex);
        }
    }

    @Override
    public String getServiceId() {
        return target.getServiceId();
    }

    private long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
