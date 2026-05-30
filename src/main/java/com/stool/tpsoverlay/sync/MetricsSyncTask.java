package com.stool.tpsoverlay.sync;

import com.stool.tpsoverlay.networking.TpsOverlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public final class MetricsSyncTask {
    private static long lastSyncMs;

    private MetricsSyncTask() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(MetricsSyncTask::onEndTick);
    }

    private static void onEndTick(MinecraftServer server) {
        long now = System.currentTimeMillis();
        int interval = TpsOverlayNetworking.pollIntervalMs();
        if (now - lastSyncMs >= interval) {
            lastSyncMs = now;
            TpsOverlayNetworking.broadcastMetrics(server);
        }
    }
}
