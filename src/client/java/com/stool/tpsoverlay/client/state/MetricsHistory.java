package com.stool.tpsoverlay.client.state;

import com.stool.tpsoverlay.client.hud.MetricsGraphHud;
import com.stool.tpsoverlay.client.hud.OverlayDragHandler;
import com.stool.tpsoverlay.config.TpsOverlayConfig;
import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;
import com.stool.tpsoverlay.config.TpsWindow;
import net.minecraft.util.Mth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

public final class MetricsHistory {
    private static final int CAPACITY = 120;

    private static final float[] TPS = new float[CAPACITY];
    private static final float[] MSPT = new float[CAPACITY];
    private static final float[] FPS = new float[CAPACITY];
    private static final float[] PING = new float[CAPACITY];

    private static int size;
    private static int head = -1;
    private static long lastSampleMs;
    private static int lastIntervalMs = 125;

    private MetricsHistory() {
    }

    public static float scrollPhase() {
        if (lastSampleMs == 0L || lastIntervalMs <= 0) {
            return 0.0f;
        }
        long elapsed = System.currentTimeMillis() - lastSampleMs;
        return Mth.clamp(elapsed / (float) lastIntervalMs, 0.0f, 1.0f);
    }

    public static int sampleIntervalMs() {
        return lastIntervalMs;
    }

    public static void tick() {
        TpsOverlayConfig config = TpsOverlayConfigHandler.getConfig();
        if (!config.graphEnabled && !OverlayDragHandler.isEditMode()) {
            return;
        }
        if (!MetricsGraphHud.hasAnySeries(config) && !OverlayDragHandler.isEditMode()) {
            return;
        }

        long now = System.currentTimeMillis();
        int interval = Math.max(50, config.pollIntervalMs / 2);
        lastIntervalMs = interval;
        if (lastSampleMs != 0L && now - lastSampleMs < interval) {
            return;
        }
        lastSampleMs = now;

        pushSample(
            readTps(config),
            ClientMetricsState.hasAnyMetrics() ? ClientMetricsState.mspt() : Float.NaN,
            Minecraft.getInstance().getFps(),
            readPing()
        );
    }

    public static int size() {
        return size;
    }

    public static float tps(int indexFromOldest) {
        return valueAt(TPS, indexFromOldest);
    }

    public static float mspt(int indexFromOldest) {
        return valueAt(MSPT, indexFromOldest);
    }

    public static float fps(int indexFromOldest) {
        return valueAt(FPS, indexFromOldest);
    }

    public static float ping(int indexFromOldest) {
        return valueAt(PING, indexFromOldest);
    }

    private static void pushSample(float tps, float mspt, float fps, float ping) {
        head = (head + 1) % CAPACITY;
        TPS[head] = tps;
        MSPT[head] = mspt;
        FPS[head] = fps;
        PING[head] = ping;
        if (size < CAPACITY) {
            size++;
        }
    }

    private static float valueAt(float[] buffer, int indexFromOldest) {
        if (size == 0 || indexFromOldest < 0 || indexFromOldest >= size) {
            return Float.NaN;
        }
        int index = head - (size - 1 - indexFromOldest);
        if (index < 0) {
            index += CAPACITY;
        }
        return buffer[index];
    }

    private static float readTps(TpsOverlayConfig config) {
        if (!ClientMetricsState.hasAnyMetrics()) {
            return Float.NaN;
        }
        return ClientMetricsState.tpsForWindow(config.tpsWindow);
    }

    private static float readPing() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null) {
            return Float.NaN;
        }
        ClientPacketListener connection = minecraft.getConnection();
        PlayerInfo info = connection.getPlayerInfo(minecraft.player.getUUID());
        return info != null ? info.getLatency() : Float.NaN;
    }
}
