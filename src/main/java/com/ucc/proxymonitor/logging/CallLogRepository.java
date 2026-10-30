package com.ucc.proxymonitor.logging;

import java.util.List;

public interface CallLogRepository {

    void save(CallLog log);

    /** Returns the logs matching the filter, newest first. */
    List<CallLog> findBy(LogFilter filter);
}
