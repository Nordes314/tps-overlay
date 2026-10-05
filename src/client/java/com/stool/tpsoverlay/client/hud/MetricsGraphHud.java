package com.stool.tpsoverlay.client.hud;

import com.stool.tpsoverlay.config.TpsOverlayConfig;
import com.stool.tpsoverlay.client.state.MetricsHistory;
import com.stool.tpsoverlay.metrics.MetricsUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public final class MetricsGraphHud {
    private static final int LABEL_WIDTH = 21;
    private static final int ROW_GAP = 1;
    private static final int LINE_ALPHA = 190;
    private static final int COLOR_MIN = 0xFF5555;
    private static final int COLOR_MAX = 0x55FF55;
    private static final int LABEL_FILL_ALPHA = 56;

    private MetricsGraphHud() {
    }

    public static boolean hasAnySeries(TpsOverlayConfig config) {
        return config.graphShowTps || config.graphShowMspt || config.graphShowFps || config.graphShowPing;
    }

    public static int contentWidth(TpsOverlayConfig config) {
        return LABEL_WIDTH + config.graphWidth + config.backgroundPadding * 2;
    }

    public static int contentHeight(TpsOverlayConfig config) {
        return config.graphHeight + config.backgroundPadding * 2;
    }

    public static void render(GuiGraphicsExtractor graphics, TpsOverlayConfig config, int screenWidth, int screenHeight) {
        List<GraphRow> rows = enabledRows(config);
        if (rows.isEmpty() && !OverlayDragHandler.isEditMode()) {
            WidgetBounds.GRAPH.clear();
            return;
        }

        int width = contentWidth(config);
        int height = contentHeight(config);
        OverlayWidgets.ensureMigrated(config, OverlayWidgetKind.GRAPH, width, height, screenWidth, screenHeight);

        int x = OverlayWidgets.anchorPixelX(config, OverlayWidgetKind.GRAPH, width, screenWidth);
        int y = OverlayWidgets.anchorPixelY(config, OverlayWidgetKind.GRAPH, height, screenHeight);
        int pad = config.backgroundPadding;

        if (config.backgroundEnabled || OverlayDragHandler.isEditMode()) {
            float opacity = OverlayDragHandler.isEditMode() ? Math.max(config.backgroundOpacity, 0.35f) : config.backgroundOpacity;
            int alpha = Math.round(opacity * 255.0f);
            OverlayBackgroundRenderer.draw(graphics, config, x, y, x + width, y + height, alpha);
        }

        if (OverlayDragHandler.isEditMode()) {
            float pulse = 0.5f + 0.5f * Mth.sin(System.currentTimeMillis() * 0.004f);
            int outlineAlpha = Math.round(Mth.lerp(pulse, 64, 255));
            graphics.outline(x, y, width, height, ARGB.color(outlineAlpha, 0xFFFF55));
        }

        if (!rows.isEmpty()) {
            Font font = Minecraft.getInstance().font;
            int plotX = x + pad + LABEL_WIDTH;
            int labelX = x + pad;
            int plotY = y + pad;
            int plotWidth = config.graphWidth;
            int plotHeight = config.graphHeight;
            int rowHeight = Math.max(4, (plotHeight - ROW_GAP * (rows.size() - 1)) / rows.size());
            float scrollPx = MetricsHistory.scrollPhase() * sampleStep(plotWidth, MetricsHistory.size());

            for (GraphRow row : rows) {
                drawRow(graphics, font, config, row, labelX, plotX, plotY, plotWidth, rowHeight, scrollPx);
                plotY += rowHeight + ROW_GAP;
            }
        }

        WidgetBounds.GRAPH.set(
            x + pad,
            y + pad,
            x,
            y,
            width,
            height,
            width,
            height,
            screenWidth,
            screenHeight,
            config.graphCenterX,
            config.graphCenterY
        );
    }

    private static float sampleStep(int plotWidth, int samples) {
        if (samples <= 1) {
            return plotWidth;
        }
        return plotWidth / (float) (samples - 1);
    }

    private static List<GraphRow> enabledRows(TpsOverlayConfig config) {
        List<GraphRow> rows = new ArrayList<>(4);
        if (config.graphShowTps) {
            rows.add(new GraphRow("TPS", MetricKind.TPS, MetricsHistory::tps, 0.0f, 20.0f, true));
        }
        if (config.graphShowMspt) {
            rows.add(new GraphRow("MS", MetricKind.MSPT, MetricsHistory::mspt, 0.0f, 50.0f, false));
        }
        if (config.graphShowFps) {
            rows.add(new GraphRow("FPS", MetricKind.FPS, MetricsHistory::fps, 0.0f, 120.0f, true));
        }
        if (config.graphShowPing) {
            rows.add(new GraphRow("Ping", MetricKind.PING, MetricsHistory::ping, 0.0f, 200.0f, false));
        }
        return rows;
    }

    private static void drawRow(
        GuiGraphicsExtractor graphics,
        Font font,
        TpsOverlayConfig config,
        GraphRow row,
        int labelX,
        int plotX,
        int plotY,
        int plotWidth,
        int plotHeight,
        float scrollPx
    ) {
        int samples = MetricsHistory.size();
        float currentValue = samples > 0 ? row.reader.read(samples - 1) : Float.NaN;
        int statusRgb = statusRgb(config, row.kind, currentValue);

        graphics.fill(labelX, plotY, plotX, plotY + plotHeight, ARGB.color(LABEL_FILL_ALPHA, statusRgb));
        graphics.text(
            font,
            row.label,
            labelX,
            plotY + (plotHeight - font.lineHeight) / 2,
            ARGB.color(220, statusRgb),
            true
        );
        graphics.fill(plotX, plotY + plotHeight - 1, plotX + plotWidth, plotY + plotHeight, ARGB.color(40, 0xFFFFFF));

        if (samples < 1) {
            return;
        }

        float step = sampleStep(plotWidth, samples);
        int plotRight = plotX + plotWidth;
        int prevX = Integer.MIN_VALUE;
        int prevY = Integer.MIN_VALUE;
        float prevValue = Float.NaN;
        float lastValue = Float.NaN;

        for (int i = 0; i < samples; i++) {
            float value = row.reader.read(i);
            if (Float.isNaN(value)) {
                continue;
            }

            float sampleX = plotRight - 1 - (samples - 1 - i) * step - scrollPx;
            int px = Math.round(sampleX);
            int py = valueToPixelY(value, row.min, row.max, plotY, plotHeight);

            if (px < plotX - 1 || px > plotRight) {
                prevX = Integer.MIN_VALUE;
                prevValue = Float.NaN;
                continue;
            }

            if (prevX != Integer.MIN_VALUE) {
                drawStepSegment(
                    graphics,
                    prevX,
                    prevY,
                    px,
                    py,
                    plotX,
                    plotRight,
                    prevValue,
                    value,
                    row.min,
                    row.max,
                    row.higherIsBetter
                );
            }

            prevX = px;
            prevY = py;
            prevValue = value;
            lastValue = value;
        }

        if (prevX != Integer.MIN_VALUE && !Float.isNaN(lastValue)) {
            int holdStart = Math.max(plotX, prevX);
            if (holdStart < plotRight) {
                int color = valueLineColor(lastValue, row.min, row.max, row.higherIsBetter);
                graphics.fill(holdStart, prevY, plotRight, prevY + 1, color);
            }
        }
    }

    private static void drawStepSegment(
        GuiGraphicsExtractor graphics,
        int x0,
        int y0,
        int x1,
        int y1,
        int plotX,
        int plotRight,
        float value0,
        float value1,
        float min,
        float max,
        boolean higherIsBetter
    ) {
        if (x1 <= x0) {
            return;
        }

        int start = Mth.clamp(x0, plotX, plotRight - 1);
        int end = Mth.clamp(x1, plotX, plotRight);
        if (end > start) {
            graphics.fill(start, y0, end, y0 + 1, valueLineColor(value0, min, max, higherIsBetter));
        }

        if (y1 != y0 && x1 >= plotX && x1 < plotRight) {
            int top = Math.min(y0, y1);
            int bottom = Math.max(y0, y1);
            graphics.fill(x1, top, x1 + 1, bottom + 1, valueLineColor(value1, min, max, higherIsBetter));
        }
    }

    private static int valueLineColor(float value, float min, float max, boolean higherIsBetter) {
        float blend = Mth.clamp((value - min) / (max - min), 0.0f, 1.0f);
        if (!higherIsBetter) {
            blend = 1.0f - blend;
        }
        return blendRedGreen(blend, LINE_ALPHA);
    }

    private static int blendRedGreen(float blend, int alpha) {
        int r = Math.round(Mth.lerp(blend, (COLOR_MIN >> 16) & 0xFF, (COLOR_MAX >> 16) & 0xFF));
        int g = Math.round(Mth.lerp(blend, (COLOR_MIN >> 8) & 0xFF, (COLOR_MAX >> 8) & 0xFF));
        int b = Math.round(Mth.lerp(blend, COLOR_MIN & 0xFF, COLOR_MAX & 0xFF));
        return ARGB.color(alpha, (r << 16) | (g << 8) | b);
    }

    private static int statusRgb(TpsOverlayConfig config, MetricKind kind, float value) {
        if (!config.colorEnabled || Float.isNaN(value)) {
            return 0xAAAAAA;
        }

        ChatFormatting formatting = switch (kind) {
            case TPS -> MetricsUtil.tpsColor(value);
            case MSPT -> MetricsUtil.msptColor(value);
            case FPS -> MetricsUtil.fpsColor(Math.round(value));
            case PING -> MetricsUtil.pingColor(Math.round(value));
        };
        return formattingRgb(formatting);
    }

    private static int formattingRgb(ChatFormatting formatting) {
        return switch (formatting) {
            case BLACK -> 0x000000;
            case DARK_BLUE -> 0x0000AA;
            case DARK_GREEN -> 0x00AA00;
            case DARK_AQUA -> 0x00AAAA;
            case DARK_RED -> 0xAA0000;
            case DARK_PURPLE -> 0xAA00AA;
            case GOLD -> 0xFFAA00;
            case GRAY -> 0xAAAAAA;
            case DARK_GRAY -> 0x555555;
            case BLUE -> 0x5555FF;
            case GREEN -> 0x55FF55;
            case AQUA -> 0x55FFFF;
            case RED -> 0xFF5555;
            case LIGHT_PURPLE -> 0xFF55FF;
            case YELLOW -> 0xFFFF55;
            case WHITE -> 0xFFFFFF;
            default -> 0xFFFFFF;
        };
    }

    private static int valueToPixelY(float value, float min, float max, int plotY, int plotHeight) {
        float normalized = Mth.clamp((value - min) / (max - min), 0.0f, 1.0f);
        return plotY + plotHeight - 1 - Math.round(normalized * (plotHeight - 1));
    }

    private enum MetricKind {
        TPS,
        MSPT,
        FPS,
        PING
    }

    private record GraphRow(String label, MetricKind kind, SeriesReader reader, float min, float max, boolean higherIsBetter) {
    }

    @FunctionalInterface
    private interface SeriesReader {
        float read(int indexFromOldest);
    }
}
