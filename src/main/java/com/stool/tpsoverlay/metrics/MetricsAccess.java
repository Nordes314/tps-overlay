package com.stool.tpsoverlay.metrics;

import net.minecraft.server.MinecraftServer;

public final class MetricsAccess {
    private MetricsAccess() {
    }

    public static TickTimeService of(MinecraftServer server) {
        return (TickTimeService) server;
    }
}
