package com.stool.tpsoverlay.client.state;

import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;
import com.stool.tpsoverlay.config.TpsWindow;
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
        ClientTpsEstimator.onDisconnect();
    }

    public static void onJoin() {
        ClientTpsEstimator.onJoin();
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

    public static boolean isEstimated() {
        return !hasServerMetrics() && ClientTpsEstimator.isReady();
    }

    public static boolean hasAnyMetrics() {
        return hasServerMetrics() || ClientTpsEstimator.isReady();
    }

    public static float mspt() {
        if (hasServerMetrics()) {
            return mspt;
        }
        if (ClientTpsEstimator.isReady()) {
            return ClientTpsEstimator.estimatedMspt(TpsOverlayConfigHandler.getConfig().tpsWindow);
        }
        return Float.NaN;
    }

    public static float tps5s() {
        if (hasServerMetrics()) {
            return tps5s;
        }
        return ClientTpsEstimator.isReady() ? ClientTpsEstimator.tps5s() : Float.NaN;
    }

    public static float tps1m() {
        if (hasServerMetrics()) {
            return tps1m;
        }
        return ClientTpsEstimator.isReady() ? ClientTpsEstimator.tps1m() : Float.NaN;
    }

    public static float tps5m() {
        if (hasServerMetrics()) {
            return tps5m;
        }
        return ClientTpsEstimator.isReady() ? ClientTpsEstimator.tps5m() : Float.NaN;
    }

    public static float tps15m() {
        if (hasServerMetrics()) {
            return tps15m;
        }
        return ClientTpsEstimator.isReady() ? ClientTpsEstimator.tps15m() : Float.NaN;
    }

    public static float tpsForWindow(TpsWindow window) {
        return switch (window) {
            case FIVE_SECONDS -> tps5s();
            case ONE_MINUTE -> tps1m();
            case FIVE_MINUTES -> tps5m();
            case FIFTEEN_MINUTES -> tps15m();
        };
    }
}
