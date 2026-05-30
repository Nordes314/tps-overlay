package com.stool.tpsoverlay.metrics;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Based on TabTPS / Paper-Server rolling TPS average (MIT License).
 */
public final class RollingAverage {
    public static final int SAMPLE_INTERVAL = 20;
    private static final long SEC_IN_NANO = 1_000_000_000L;
    public static final BigDecimal TPS_BASE = new BigDecimal("1E9").multiply(new BigDecimal(SAMPLE_INTERVAL));

    private final int size;
    private final BigDecimal[] samples;
    private final long[] times;
    private long time;
    private BigDecimal total;
    private int index;

    public RollingAverage(int size) {
        this.size = size;
        this.samples = new BigDecimal[size];
        this.times = new long[size];
        this.time = (long) size * SEC_IN_NANO;
        this.total = dec(20).multiply(dec(SEC_IN_NANO)).multiply(dec(size));
        for (int i = 0; i < size; i++) {
            this.samples[i] = dec(20);
            this.times[i] = SEC_IN_NANO;
        }
    }

    private static BigDecimal dec(long value) {
        return new BigDecimal(value);
    }

    public void add(BigDecimal value, long durationNanos) {
        this.time -= this.times[this.index];
        this.total = this.total.subtract(this.samples[this.index].multiply(dec(this.times[this.index])));
        this.samples[this.index] = value;
        this.times[this.index] = durationNanos;
        this.time += durationNanos;
        this.total = this.total.add(value.multiply(dec(durationNanos)));
        if (++this.index == this.size) {
            this.index = 0;
        }
    }

    public double average() {
        return this.total.divide(dec(this.time), 30, RoundingMode.HALF_UP).doubleValue();
    }
}
