package com.stool.tpsoverlay.client.hud;

import com.stool.tpsoverlay.config.TpsOverlayConfig;
import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import java.util.Locale;

public final class TpsOverlayHud {
    private TpsOverlayHud() {
    }

    public static void render(GuiGraphicsExtractor graphics, float tickDelta) {
        Minecraft minecraft = Minecraft.getInstance();
        TpsOverlayConfig config = TpsOverlayConfigHandler.getConfig();
        boolean editMode = OverlayDragHandler.isEditMode();

        if (minecraft.player == null || minecraft.level == null) {
            WidgetBounds.TEXT.clear();
            WidgetBounds.GRAPH.clear();
            return;
        }

        boolean hidden = shouldHide(minecraft, config);
        boolean showText = editMode || (config.enabled && !hidden);
        boolean showGraph = editMode || (config.graphEnabled && MetricsGraphHud.hasAnySeries(config) && !hidden);

        if (!showText && !showGraph) {
            WidgetBounds.TEXT.clear();
            WidgetBounds.GRAPH.clear();
            return;
        }

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        var font = minecraft.font;

        if (showText) {
            renderTextOverlay(graphics, config, font, screenWidth, screenHeight, editMode);
        } else {
            WidgetBounds.TEXT.clear();
        }

        if (showGraph) {
            MetricsGraphHud.render(graphics, config, screenWidth, screenHeight);
        } else {
            WidgetBounds.GRAPH.clear();
        }

        if (editMode) {
            renderEditOverlay(graphics, font, config, screenWidth, screenHeight);
        }
    }

    private static void renderTextOverlay(
        GuiGraphicsExtractor graphics,
        TpsOverlayConfig config,
        net.minecraft.client.gui.Font font,
        int screenWidth,
        int screenHeight,
        boolean editMode
    ) {
        if (!config.enabled && !editMode) {
            WidgetBounds.TEXT.clear();
            return;
        }

        Component text = FormatRenderer.render(config);
        float scale = Math.max(0.5f, Math.min(2.0f, config.textScale));
        int textWidth = Math.round(font.width(text) * scale);
        int textHeight = Math.round(font.lineHeight * scale);

        OverlayWidgets.ensureMigrated(config, OverlayWidgetKind.TEXT, textWidth, textHeight, screenWidth, screenHeight);

        int x = OverlayWidgets.anchorPixelX(config, OverlayWidgetKind.TEXT, textWidth, screenWidth);
        int y = OverlayWidgets.anchorPixelY(config, OverlayWidgetKind.TEXT, textHeight, screenHeight);
        int pad = Math.round(config.backgroundPadding * scale);

        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(scale, scale);

        int localPad = config.backgroundPadding;
        int localTextWidth = font.width(text);
        int localTextHeight = font.lineHeight;

        if (config.backgroundEnabled || editMode) {
            float opacity = editMode ? Math.max(config.backgroundOpacity, 0.35f) : config.backgroundOpacity;
            int alpha = Math.round(opacity * 255.0f);
            OverlayBackgroundRenderer.draw(
                graphics,
                config,
                -localPad,
                -localPad,
                localTextWidth + localPad,
                localTextHeight + localPad,
                alpha
            );
        }

        if (editMode) {
            float pulse = 0.5f + 0.5f * Mth.sin(System.currentTimeMillis() * 0.004f);
            int outlineAlpha = Math.round(Mth.lerp(pulse, 64, 255));
            graphics.outline(-localPad, -localPad, localTextWidth + localPad * 2, localTextHeight + localPad * 2, ARGB.color(outlineAlpha, 0xFFFF55));
        }

        graphics.text(font, text, 0, 0, ARGB.color(255, 0xFFFFFF), true);
        pose.popMatrix();

        WidgetBounds.TEXT.set(
            x,
            y,
            x - pad,
            y - pad,
            textWidth + pad * 2,
            textHeight + pad * 2,
            textWidth,
            textHeight,
            screenWidth,
            screenHeight,
            config.overlayX,
            config.overlayY
        );
    }

    private static void renderEditOverlay(GuiGraphicsExtractor graphics, Font font, TpsOverlayConfig config, int screenWidth, int screenHeight) {
        int guideColor = ARGB.color(160, 0xFFFF55);

        if (OverlayEditState.guideX()) {
            int centerX = screenWidth / 2;
            graphics.fill(centerX, 0, centerX + 1, screenHeight, guideColor);
        }
        if (OverlayEditState.guideY()) {
            int centerY = screenHeight / 2;
            graphics.fill(0, centerY, screenWidth, centerY + 1, guideColor);
        }

        float labelScale = 0.75f;
        int margin = 4;
        int lineHeight = Math.round(font.lineHeight * labelScale);
        int lines = 2;
        int labelY = screenHeight - margin - lineHeight * lines;

        drawPositionLabel(graphics, font, labelScale, margin, labelY,
            String.format(Locale.ROOT, "text: x:%.1f | y:%.1f", config.overlayX, config.overlayY));
        drawPositionLabel(graphics, font, labelScale, margin, labelY + lineHeight,
            String.format(Locale.ROOT, "graph: x:%.1f | y:%.1f", config.graphCenterX, config.graphCenterY));
    }

    private static void drawPositionLabel(GuiGraphicsExtractor graphics, Font font, float scale, int margin, int y, String label) {
        int screenWidth = graphics.guiWidth();
        int labelWidth = Math.round(font.width(label) * scale);
        int labelX = screenWidth - labelWidth - margin;

        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(labelX, y);
        pose.scale(scale, scale);
        graphics.text(font, label, 0, 0, ARGB.color(200, 0xAAAAAA), true);
        pose.popMatrix();
    }

    static boolean shouldHide(Minecraft minecraft, TpsOverlayConfig config) {
        if (config.hideInMenus && minecraft.screen != null && !(minecraft.screen instanceof ChatScreen)) {
            return true;
        }
        if (config.hideWhenChatOpen && minecraft.screen instanceof ChatScreen) {
            return true;
        }
        if (config.hideWithF3 && minecraft.getDebugOverlay().showDebugScreen()) {
            return true;
        }
        if (config.hideInCinematic && minecraft.options.hideGui) {
            return true;
        }
        return false;
    }
}
