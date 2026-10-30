package com.ucc.proxymonitor.service;

import java.util.Random;

/**
 * Decides randomly whether a call must fail on purpose (e.g. 0.10 = 10%).
 */
public class FailureInjector {

    private final double failureRate;
    private final Random random;

    public FailureInjector(double failureRate, Random random) {
        this.failureRate = failureRate;
        this.random = random;
    }

    public boolean shouldFail() {
        return random.nextDouble() < failureRate;
    }

    public double getFailureRate() {
        return failureRate;
    }
}
