package com.stool.tpsoverlay.client.hud;

final class OverlayEditSnapping {
    static final int THRESHOLD_PX = 12;

    private OverlayEditSnapping() {
    }

    static SnappedPosition snap(float centerX, float centerY, int contentWidth, int contentHeight, int screenWidth, int screenHeight) {
        float snappedX = centerX;
        float snappedY = centerY;
        boolean guideX = false;
        boolean guideY = false;

        if (screenWidth > 0 && Math.abs(centerX - 0.5f) * screenWidth <= THRESHOLD_PX) {
            snappedX = 0.5f;
            guideX = true;
        }

        if (screenHeight > 0 && Math.abs(centerY - 0.5f) * screenHeight <= THRESHOLD_PX) {
            snappedY = 0.5f;
            guideY = true;
        }

        return new SnappedPosition(
            OverlayPosition.clampCenterX(snappedX, contentWidth, screenWidth),
            OverlayPosition.clampCenterY(snappedY, contentHeight, screenHeight),
            guideX,
            guideY
        );
    }

    record SnappedPosition(float centerX, float centerY, boolean guideX, boolean guideY) {
    }
}
