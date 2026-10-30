package com.ucc.proxymonitor.logging;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a short version of a stack trace: the exception line plus the first N frames.
 */
public class StackTraceSummarizer {

    private final int maxFrames;

    public StackTraceSummarizer(int maxFrames) {
        this.maxFrames = maxFrames;
    }

    public List<String> summarize(Throwable error) {
        List<String> lines = new ArrayList<>();
        lines.add(error.getClass().getName() + ": " + error.getMessage());

        StackTraceElement[] frames = error.getStackTrace();
        int limit = Math.min(maxFrames, frames.length);
        for (int i = 0; i < limit; i++) {
            lines.add("at " + frames[i]);
        }
        if (frames.length > limit) {
            lines.add("... " + (frames.length - limit) + " more");
        }
        return lines;
    }
}
