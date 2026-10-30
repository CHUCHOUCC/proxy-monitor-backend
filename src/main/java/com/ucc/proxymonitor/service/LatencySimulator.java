package com.ucc.proxymonitor.service;

import java.util.Random;

/**
 * Adds a random delay to imitate network / processing time of a real microservice.
 */
public class LatencySimulator {

    private final int minMs;
    private final int maxMs;
    private final Random random;

    public LatencySimulator(int minMs, int maxMs, Random random) {
        this.minMs = minMs;
        this.maxMs = maxMs;
        this.random = random;
    }

    public static LatencySimulator none() {
        return new LatencySimulator(0, 0, new Random());
    }

    public void pause() {
        if (maxMs <= 0) {
            return;
        }
        try {
            Thread.sleep(minMs + random.nextInt(maxMs - minMs + 1));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
