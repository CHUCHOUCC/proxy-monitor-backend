package com.ucc.proxymonitor.logging;

import com.ucc.proxymonitor.exception.InvalidArgumentsException;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Criteria used to filter logs. Any null field means "no filter".
 */
public record LogFilter(String serviceId, CallStatus status, Instant from, Instant to) {

    public static LogFilter all() {
        return new LogFilter(null, null, null, null);
    }

    public static LogFilter forService(String serviceId) {
        return new LogFilter(serviceId, null, null, null);
    }

    /** Builds a filter from raw query-string values (blank values are ignored). */
    public static LogFilter fromQuery(String service, String status, String from, String to) {
        return new LogFilter(
                isBlank(service) ? null : service.trim(),
                isBlank(status) ? null : parseStatus(status),
                isBlank(from) ? null : parseInstant(from, "from"),
                isBlank(to) ? null : parseInstant(to, "to"));
    }

    public boolean matches(CallLog log) {
        return (serviceId == null || serviceId.equals(log.getServiceId()))
                && (status == null || status == log.getStatus())
                && (from == null || !log.getTimestamp().isBefore(from))
                && (to == null || !log.getTimestamp().isAfter(to));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static CallStatus parseStatus(String value) {
        try {
            return CallStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidArgumentsException("Invalid status '" + value + "'. Use SUCCESS or ERROR");
        }
    }

    private static Instant parseInstant(String value, String name) {
        try {
            return Instant.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new InvalidArgumentsException("Invalid '" + name + "' date. Use ISO-8601, e.g. 2026-10-30T15:00:00Z");
        }
    }
}
