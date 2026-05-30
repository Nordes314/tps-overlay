package com.stool.tpsoverlay.metrics;

/**
 * Based on TabTPS / Paper-Server tick times buffer (MIT License).
 */
public final class TickTimes {
    private final long[] times;

    public TickTimes(int length) {
        this.times = new long[length];
    }

    public void add(int index, long time) {
        this.times[index % this.times.length] = time;
    }
}
