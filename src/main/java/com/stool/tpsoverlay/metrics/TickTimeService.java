package com.stool.tpsoverlay.metrics;

public interface TickTimeService {
    double averageMspt();

    double[] recentTps();

    default double displayTps() {
        double[] recentTps = recentTps();
        if (recentTps.length >= 4) {
            return recentTps[0];
        }
        if (recentTps.length >= 2) {
            return recentTps[1];
        }
        return recentTps.length > 0 ? recentTps[0] : 0.0;
    }
}
