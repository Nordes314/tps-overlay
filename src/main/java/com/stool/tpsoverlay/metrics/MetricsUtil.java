package com.stool.tpsoverlay.metrics;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.text.DecimalFormat;

public final class MetricsUtil {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.00");

    private MetricsUtil() {
    }

    public static String formatDouble(double value) {
        return FORMAT.format(value);
    }

    public static double toMilliseconds(long nanos) {
        return nanos * 1.0E-6D;
    }

    public static double toMilliseconds(double nanos) {
        return nanos * 1.0E-6D;
    }

    public static double average(long[] values) {
        long sum = 0L;
        for (long value : values) {
            sum += value;
        }
        return sum / (double) values.length;
    }

    public static ChatFormatting tpsColor(double tps) {
        if (tps >= 18.5) {
            return ChatFormatting.GREEN;
        }
        if (tps > 15.0) {
            return ChatFormatting.GOLD;
        }
        return ChatFormatting.RED;
    }

    public static ChatFormatting msptColor(double mspt) {
        if (mspt <= 25.0) {
            return ChatFormatting.GREEN;
        }
        if (mspt <= 40.0) {
            return ChatFormatting.GOLD;
        }
        return ChatFormatting.RED;
    }

    public static ChatFormatting pingColor(int ping) {
        if (ping < 100) {
            return ChatFormatting.GREEN;
        }
        if (ping < 250) {
            return ChatFormatting.GOLD;
        }
        return ChatFormatting.RED;
    }

    public static ChatFormatting fpsColor(int fps) {
        if (fps >= 60) {
            return ChatFormatting.GREEN;
        }
        if (fps >= 30) {
            return ChatFormatting.GOLD;
        }
        return ChatFormatting.RED;
    }

    public static MutableComponent coloredValue(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color);
    }

    public static MutableComponent coloredTps(double tps, boolean colorEnabled) {
        String text = formatDouble(tps);
        return colorEnabled ? coloredValue(text, tpsColor(tps)) : Component.literal(text);
    }

    public static MutableComponent coloredMspt(double mspt, boolean colorEnabled) {
        String text = formatDouble(mspt);
        return colorEnabled ? coloredValue(text, msptColor(mspt)) : Component.literal(text);
    }

    public static MutableComponent coloredPing(int ping, boolean colorEnabled) {
        String text = String.valueOf(ping);
        return colorEnabled ? coloredValue(text, pingColor(ping)) : Component.literal(text);
    }

    public static MutableComponent coloredFps(int fps, boolean colorEnabled) {
        String text = String.valueOf(fps);
        return colorEnabled ? coloredValue(text, fpsColor(fps)) : Component.literal(text);
    }
}
