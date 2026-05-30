package com.stool.tpsoverlay.client.state;

import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;
import com.stool.tpsoverlay.config.TpsWindow;
import com.stool.tpsoverlay.metrics.RollingAverage;
import net.minecraft.util.Mth;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ClientTpsEstimator {
    private static final int WARMUP_MS = 4000;

    private static RollingAverage tps5s = new RollingAverage(5);
    private static RollingAverage tps1m = new RollingAverage(60);
    private static RollingAverage tps5m = new RollingAverage(60 * 5);
    private static RollingAverage tps15m = new RollingAverage(60 * 15);

    private static long lastPacketMs = -1L;
    private static long joinedMs;
    private static boolean hasSample;

    private ClientTpsEstimator() {
    }

    public static void onJoin() {
        reset();
        joinedMs = System.currentTimeMillis();
    }

    public static void onDisconnect() {
        reset();
    }

    public static void onTimePacket() {
        if (!TpsOverlayConfigHandler.getConfig().estimateClientTps) {
            return;
        }

        long now = System.currentTimeMillis();
        if (lastPacketMs < 0L) {
            lastPacketMs = now;
            return;
        }

        long elapsedMs = now - lastPacketMs;
        lastPacketMs = now;
        if (elapsedMs <= 0L) {
            return;
        }

        long elapsedNanos = elapsedMs * 1_000_000L;
        BigDecimal currentTps = RollingAverage.TPS_BASE.divide(new BigDecimal(elapsedNanos), 30, RoundingMode.HALF_UP);
        double tps = Mth.clamp(currentTps.doubleValue(), 0.0, 20.0);
        BigDecimal sample = BigDecimal.valueOf(tps);
        tps5s.add(sample, elapsedNanos);
        tps1m.add(sample, elapsedNanos);
        tps5m.add(sample, elapsedNanos);
        tps15m.add(sample, elapsedNanos);
        hasSample = true;
    }

    public static boolean isEnabled() {
        return TpsOverlayConfigHandler.getConfig().estimateClientTps;
    }

    public static boolean isReady() {
        return isEnabled()
            && hasSample
            && joinedMs > 0L
            && System.currentTimeMillis() - joinedMs >= WARMUP_MS;
    }

    public static float tps5s() {
        return (float) tps5s.average();
    }

    public static float tps1m() {
        return (float) tps1m.average();
    }

    public static float tps5m() {
        return (float) tps5m.average();
    }

    public static float tps15m() {
        return (float) tps15m.average();
    }

    public static float tpsForWindow(TpsWindow window) {
        return switch (window) {
            case FIVE_SECONDS -> tps5s();
            case ONE_MINUTE -> tps1m();
            case FIVE_MINUTES -> tps5m();
            case FIFTEEN_MINUTES -> tps15m();
        };
    }

    public static float estimatedMspt(TpsWindow window) {
        float tps = tpsForWindow(window);
        return tps > 0.0f ? 1000.0f / tps : Float.NaN;
    }

    private static void reset() {
        tps5s = new RollingAverage(5);
        tps1m = new RollingAverage(60);
        tps5m = new RollingAverage(60 * 5);
        tps15m = new RollingAverage(60 * 15);
        lastPacketMs = -1L;
        joinedMs = 0L;
        hasSample = false;
    }
}
