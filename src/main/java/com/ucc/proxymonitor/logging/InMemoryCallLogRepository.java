package com.ucc.proxymonitor.logging;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Thread-safe in-memory storage. Keeps the newest logs first and discards
 * the oldest ones once maxSize is reached.
 */
public class InMemoryCallLogRepository implements CallLogRepository {

    private final Deque<CallLog> logs = new ArrayDeque<>();
    private final int maxSize;

    public InMemoryCallLogRepository(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public synchronized void save(CallLog log) {
        logs.addFirst(log);
        if (logs.size() > maxSize) {
            logs.removeLast();
        }
    }

    @Override
    public synchronized List<CallLog> findBy(LogFilter filter) {
        return logs.stream().filter(filter::matches).toList();
    }
}
