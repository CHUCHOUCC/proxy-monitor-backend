package com.ucc.proxymonitor.logging;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Immutable record of one call intercepted by the LoggingProxy.
 */
public class CallLog {

    private final String requestId;
    private final String serviceId;
    private final String operation;
    private final long durationMs;
    private final CallStatus status;
    private final Instant timestamp;
    private final List<Object> params;
    private final Object response;
    private final String errorType;
    private final String errorMessage;
    private final List<String> stackTrace;

    private CallLog(String requestId, String serviceId, String operation, long durationMs, CallStatus status,
                    Instant timestamp, Object[] params, Object response, String errorType, String errorMessage,
                    List<String> stackTrace) {
        this.requestId = requestId;
        this.serviceId = serviceId;
        this.operation = operation;
        this.durationMs = durationMs;
        this.status = status;
        this.timestamp = timestamp;
        this.params = params == null ? List.of() : Collections.unmodifiableList(Arrays.asList(params.clone()));
        this.response = response;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace == null ? List.of() : List.copyOf(stackTrace);
    }

    public static CallLog success(String requestId, String serviceId, String operation, long durationMs,
                                  Instant timestamp, Object[] params, Object response) {
        return new CallLog(requestId, serviceId, operation, durationMs, CallStatus.SUCCESS,
                timestamp, params, response, null, null, null);
    }

    public static CallLog error(String requestId, String serviceId, String operation, long durationMs,
                                Instant timestamp, Object[] params, Throwable error, List<String> stackTrace) {
        return new CallLog(requestId, serviceId, operation, durationMs, CallStatus.ERROR,
                timestamp, params, null, error.getClass().getSimpleName(), error.getMessage(), stackTrace);
    }

    public boolean isSuccess() {
        return status == CallStatus.SUCCESS;
    }

    public String getRequestId() { return requestId; }
    public String getServiceId() { return serviceId; }
    public String getOperation() { return operation; }
    public long getDurationMs() { return durationMs; }
    public CallStatus getStatus() { return status; }
    public Instant getTimestamp() { return timestamp; }
    public List<Object> getParams() { return params; }
    public Object getResponse() { return response; }
    public String getErrorType() { return errorType; }
    public String getErrorMessage() { return errorMessage; }
    public List<String> getStackTrace() { return stackTrace; }
}
