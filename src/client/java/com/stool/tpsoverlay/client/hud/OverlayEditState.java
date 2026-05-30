package com.stool.tpsoverlay.client.hud;

public final class OverlayEditState {
    private static boolean dragging;
    private static boolean guideX;
    private static boolean guideY;

    private OverlayEditState() {
    }

    public static boolean isDragging() {
        return dragging;
    }

    public static void setDragging(boolean value) {
        dragging = value;
        if (!value) {
            guideX = false;
            guideY = false;
        }
    }

    public static boolean guideX() {
        return guideX;
    }

    public static boolean guideY() {
        return guideY;
    }

    public static void setGuides(boolean verticalCenterLine, boolean horizontalCenterLine) {
        guideX = verticalCenterLine;
        guideY = horizontalCenterLine;
    }
}
