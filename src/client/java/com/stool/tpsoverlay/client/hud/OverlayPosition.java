package com.stool.tpsoverlay.client.hud;

import com.stool.tpsoverlay.config.TpsOverlayConfig;
import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;
import net.minecraft.util.Mth;

final class OverlayPosition {
    private OverlayPosition() {
    }

    static void ensureMigrated(TpsOverlayConfig config, int contentWidth, int contentHeight, int screenWidth, int screenHeight) {
        if (config.centerAnchor || screenWidth <= 0 || screenHeight <= 0) {
            return;
        }

        config.overlayX = anchorToCenterX(config.overlayX, contentWidth, screenWidth);
        config.overlayY = anchorToCenterY(config.overlayY, contentHeight, screenHeight);
        config.centerAnchor = true;
        TpsOverlayConfigHandler.save();
    }

    static int anchorPixelX(TpsOverlayConfig config, int contentWidth, int screenWidth) {
        return Math.round(config.overlayX * screenWidth - contentWidth * 0.5f);
    }

    static int anchorPixelY(TpsOverlayConfig config, int contentHeight, int screenHeight) {
        return Math.round(config.overlayY * screenHeight - contentHeight * 0.5f);
    }

    static float centerFromPixelX(double pixelX, int screenWidth) {
        if (screenWidth <= 0) {
            return 0.0f;
        }
        return (float) (pixelX / screenWidth);
    }

    static float centerFromPixelY(double pixelY, int screenHeight) {
        if (screenHeight <= 0) {
            return 0.0f;
        }
        return (float) (pixelY / screenHeight);
    }

    static float clampCenterX(float centerX, int contentWidth, int screenWidth) {
        if (screenWidth <= 0) {
            return Mth.clamp(centerX, 0.0f, 1.0f);
        }
        float half = contentWidth / (2.0f * screenWidth);
        return Mth.clamp(centerX, half, 1.0f - half);
    }

    static float clampCenterY(float centerY, int contentHeight, int screenHeight) {
        if (screenHeight <= 0) {
            return Mth.clamp(centerY, 0.0f, 1.0f);
        }
        float half = contentHeight / (2.0f * screenHeight);
        return Mth.clamp(centerY, half, 1.0f - half);
    }

    private static float anchorToCenterX(float anchorX, int contentWidth, int screenWidth) {
        return anchorX + contentWidth / (2.0f * screenWidth);
    }

    private static float anchorToCenterY(float anchorY, int contentHeight, int screenHeight) {
        return anchorY + contentHeight / (2.0f * screenHeight);
    }
}
