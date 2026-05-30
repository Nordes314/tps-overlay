package com.stool.tpsoverlay.client.state;

import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;
import com.stool.tpsoverlay.networking.MetricsPayload;

public final class ClientMetricsState {
    private static float mspt;
    private static float tps5s;
    private static float tps1m;
    private static float tps5m;
    private static float tps15m;
    private static long lastUpdateMs;
    private static boolean serverHasMod;

    private ClientMetricsState() {
    }

    public static void update(MetricsPayload payload) {
        mspt = payload.mspt();
        tps5s = payload.tps5s();
        tps1m = payload.tps1m();
        tps5m = payload.tps5m();
        tps15m = payload.tps15m();
        lastUpdateMs = System.currentTimeMillis();
        serverHasMod = true;
    }

    public static void onDisconnect() {
        serverHasMod = false;
        lastUpdateMs = 0L;
    }

    public static void tickAvailability() {
        if (!serverHasMod) {
            return;
        }
        int timeout = Math.max(150, TpsOverlayConfigHandler.getConfig().pollIntervalMs * 3);
        if (lastUpdateMs > 0L && System.currentTimeMillis() - lastUpdateMs > timeout) {
            serverHasMod = false;
        }
    }

    public static boolean hasServerMetrics() {
        tickAvailability();
        return serverHasMod;
    }

    public static float mspt() {
        return mspt;
    }

    public static float tps5s() {
        return tps5s;
    }

    public static float tps1m() {
        return tps1m;
    }

    public static float tps5m() {
        return tps5m;
    }

    public static float tps15m() {
        return tps15m;
    }
}
