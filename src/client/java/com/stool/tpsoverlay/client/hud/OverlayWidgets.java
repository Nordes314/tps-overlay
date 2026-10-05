package com.stool.tpsoverlay.client.hud;

import com.stool.tpsoverlay.config.TpsOverlayConfig;
import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;

public final class OverlayWidgets {
    private OverlayWidgets() {
    }

    static float centerX(TpsOverlayConfig config, OverlayWidgetKind kind) {
        return switch (kind) {
            case TEXT -> config.overlayX;
            case GRAPH -> config.graphCenterX;
        };
    }

    static float centerY(TpsOverlayConfig config, OverlayWidgetKind kind) {
        return switch (kind) {
            case TEXT -> config.overlayY;
            case GRAPH -> config.graphCenterY;
        };
    }

    static void setCenter(TpsOverlayConfig config, OverlayWidgetKind kind, float centerX, float centerY) {
        switch (kind) {
            case TEXT -> {
                config.overlayX = centerX;
                config.overlayY = centerY;
            }
            case GRAPH -> {
                config.graphCenterX = centerX;
                config.graphCenterY = centerY;
            }
        }
    }

    static boolean centerAnchor(TpsOverlayConfig config, OverlayWidgetKind kind) {
        return switch (kind) {
            case TEXT -> config.centerAnchor;
            case GRAPH -> config.graphCenterAnchor;
        };
    }

    static void setCenterAnchor(TpsOverlayConfig config, OverlayWidgetKind kind, boolean value) {
        switch (kind) {
            case TEXT -> config.centerAnchor = value;
            case GRAPH -> config.graphCenterAnchor = value;
        }
    }

    static void ensureMigrated(TpsOverlayConfig config, OverlayWidgetKind kind, int contentWidth, int contentHeight, int screenWidth, int screenHeight) {
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }

        float anchorX = centerX(config, kind);
        float anchorY = centerY(config, kind);
        float centerX = centerAnchor(config, kind)
            ? OverlayPosition.clampCenterX(anchorX, contentWidth, screenWidth)
            : OverlayPosition.clampCenterX(anchorX + contentWidth / (2.0f * screenWidth), contentWidth, screenWidth);
        float centerY = centerAnchor(config, kind)
            ? OverlayPosition.clampCenterY(anchorY, contentHeight, screenHeight)
            : OverlayPosition.clampCenterY(anchorY + contentHeight / (2.0f * screenHeight), contentHeight, screenHeight);

        if (centerAnchor(config, kind) && centerX == anchorX && centerY == anchorY) {
            return;
        }

        setCenter(config, kind, centerX, centerY);
        if (!centerAnchor(config, kind)) {
            setCenterAnchor(config, kind, true);
        }
        TpsOverlayConfigHandler.save();
    }

    static int anchorPixelX(TpsOverlayConfig config, OverlayWidgetKind kind, int contentWidth, int screenWidth) {
        return Math.round(centerX(config, kind) * screenWidth - contentWidth * 0.5f);
    }

    static int anchorPixelY(TpsOverlayConfig config, OverlayWidgetKind kind, int contentHeight, int screenHeight) {
        return Math.round(centerY(config, kind) * screenHeight - contentHeight * 0.5f);
    }

    public static void reset(TpsOverlayConfig config, TpsOverlayConfig defaults, OverlayWidgetKind kind) {
        switch (kind) {
            case TEXT -> {
                config.overlayX = defaults.overlayX;
                config.overlayY = defaults.overlayY;
                config.centerAnchor = defaults.centerAnchor;
            }
            case GRAPH -> {
                config.graphCenterX = defaults.graphCenterX;
                config.graphCenterY = defaults.graphCenterY;
                config.graphCenterAnchor = defaults.graphCenterAnchor;
            }
        }
    }
}
