package com.stool.tpsoverlay.client.hud;

import com.stool.tpsoverlay.client.state.ClientMetricsState;
import com.stool.tpsoverlay.config.TpsOverlayConfig;
import com.stool.tpsoverlay.config.TpsWindow;
import com.stool.tpsoverlay.metrics.MetricsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FormatRenderer {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(tps|mspt|ping|fps|tps_5s|tps_1m|tps_5m|tps_15m|entities|entities_visible)\\}");

    private FormatRenderer() {
    }

    public static Component render(TpsOverlayConfig config) {
        String format = config.displayFormat.replace("\\n", "\n");
        Matcher matcher = PLACEHOLDER.matcher(format);
        List<Component> parts = new ArrayList<>();
        int last = 0;
        while (matcher.find()) {
            if (matcher.start() > last) {
                parts.add(literal(format.substring(last, matcher.start()), ChatFormatting.GRAY));
            }
            parts.add(resolvePlaceholder(matcher.group(1), config));
            last = matcher.end();
        }
        if (last < format.length()) {
            parts.add(literal(format.substring(last), ChatFormatting.GRAY));
        }
        MutableComponent result = Component.empty();
        for (Component part : parts) {
            result.append(part);
        }
        return result;
    }

    private static Component resolvePlaceholder(String key, TpsOverlayConfig config) {
        return switch (key.toLowerCase(Locale.ROOT)) {
            case "tps" -> renderTps(config, selectedTps(config));
            case "tps_5s" -> renderTps(config, ClientMetricsState.tps5s());
            case "tps_1m" -> renderTps(config, ClientMetricsState.tps1m());
            case "tps_5m" -> renderTps(config, ClientMetricsState.tps5m());
            case "tps_15m" -> renderTps(config, ClientMetricsState.tps15m());
            case "mspt" -> renderMspt(config, ClientMetricsState.mspt());
            case "ping" -> MetricsUtil.coloredPing(readPing(), config.colorEnabled);
            case "fps" -> MetricsUtil.coloredFps(Minecraft.getInstance().getFps(), config.colorEnabled);
            case "entities" -> renderCount(readEntityCount());
            case "entities_visible" -> renderCount(readVisibleEntityCount());
            default -> Component.literal("{" + key + "}");
        };
    }

    private static Component renderTps(TpsOverlayConfig config, float value) {
        if (!ClientMetricsState.hasServerMetrics()) {
            return Component.literal("N/A").withStyle(ChatFormatting.GRAY);
        }
        return MetricsUtil.coloredTps(value, config.colorEnabled);
    }

    private static Component renderMspt(TpsOverlayConfig config, float value) {
        if (!ClientMetricsState.hasServerMetrics()) {
            return Component.literal("N/A").withStyle(ChatFormatting.GRAY);
        }
        return MetricsUtil.coloredMspt(value, config.colorEnabled);
    }

    private static float selectedTps(TpsOverlayConfig config) {
        return switch (config.tpsWindow) {
            case FIVE_SECONDS -> ClientMetricsState.tps5s();
            case ONE_MINUTE -> ClientMetricsState.tps1m();
            case FIVE_MINUTES -> ClientMetricsState.tps5m();
            case FIFTEEN_MINUTES -> ClientMetricsState.tps15m();
        };
    }

    private static int readPing() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null) {
            return 0;
        }
        ClientPacketListener connection = minecraft.getConnection();
        PlayerInfo info = connection.getPlayerInfo(minecraft.player.getUUID());
        return info != null ? info.getLatency() : 0;
    }

    private static int readEntityCount() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.level instanceof ClientLevel level)) {
            return 0;
        }
        return level.getEntityCount();
    }

    private static int readVisibleEntityCount() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.level instanceof ClientLevel level)) {
            return 0;
        }
        int count = 0;
        for (Entity ignored : level.entitiesForRendering()) {
            count++;
        }
        return count;
    }

    private static Component renderCount(int value) {
        return Component.literal(String.valueOf(value)).withStyle(ChatFormatting.WHITE);
    }

    private static MutableComponent literal(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color);
    }
}
