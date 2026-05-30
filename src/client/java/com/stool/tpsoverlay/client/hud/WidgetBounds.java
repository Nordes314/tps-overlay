package com.stool.tpsoverlay.client.hud;

public final class WidgetBounds {
    public static final WidgetBounds TEXT = new WidgetBounds();
    public static final WidgetBounds GRAPH = new WidgetBounds();

    private int anchorX;
    private int anchorY;
    private int x;
    private int y;
    private int width;
    private int height;
    private int contentWidth;
    private int contentHeight;
    private int screenWidth;
    private int screenHeight;
    private float centerX;
    private float centerY;
    private boolean valid;

    private WidgetBounds() {
    }

    public void set(
        int anchorX,
        int anchorY,
        int x,
        int y,
        int width,
        int height,
        int contentWidth,
        int contentHeight,
        int screenWidth,
        int screenHeight,
        float centerX,
        float centerY
    ) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.contentWidth = contentWidth;
        this.contentHeight = contentHeight;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.centerX = centerX;
        this.centerY = centerY;
        this.valid = true;
    }

    public void clear() {
        this.valid = false;
    }

    public boolean valid() {
        return valid;
    }

    public boolean containsWithSlop(double mouseX, double mouseY, int slop) {
        return valid
            && mouseX >= x - slop && mouseX <= x + width + slop
            && mouseY >= y - slop && mouseY <= y + height + slop;
    }

    public int contentWidth() {
        return contentWidth;
    }

    public int contentHeight() {
        return contentHeight;
    }

    public int screenWidth() {
        return screenWidth;
    }

    public int screenHeight() {
        return screenHeight;
    }

    public double centerPixelX() {
        return centerX * screenWidth;
    }

    public double centerPixelY() {
        return centerY * screenHeight;
    }

    public static OverlayWidgetKind hitTest(double mouseX, double mouseY) {
        if (GRAPH.valid() && GRAPH.containsWithSlop(mouseX, mouseY, 6)) {
            return OverlayWidgetKind.GRAPH;
        }
        if (TEXT.valid() && TEXT.containsWithSlop(mouseX, mouseY, 6)) {
            return OverlayWidgetKind.TEXT;
        }
        return null;
    }
}
