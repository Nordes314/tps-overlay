package com.stool.tpsoverlay.client.hud;

import com.mojang.blaze3d.platform.InputConstants;
import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class OverlayEditScreen extends Screen {
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 8;

    private final float savedTextX;
    private final float savedTextY;
    private final float savedGraphX;
    private final float savedGraphY;
    private OverlayWidgetKind draggingWidget;
    private double dragOffsetX;
    private double dragOffsetY;

    public OverlayEditScreen() {
        super(Component.empty());
        var config = TpsOverlayConfigHandler.getConfig();
        savedTextX = config.overlayX;
        savedTextY = config.overlayY;
        savedGraphX = config.graphCenterX;
        savedGraphY = config.graphCenterY;
    }

    @Override
    protected void init() {
        int totalWidth = BUTTON_WIDTH * 2 + BUTTON_GAP;
        int startX = (width - totalWidth) / 2;
        int y = height / 2 - BUTTON_HEIGHT / 2;

        addRenderableWidget(Button.builder(Component.translatable("tpsoverlay.edit.reset"), button -> resetPosition())
            .bounds(startX, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build());
        addRenderableWidget(Button.builder(Component.translatable("tpsoverlay.edit.save"), button -> saveAndClose())
            .bounds(startX + BUTTON_WIDTH + BUTTON_GAP, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void onClose() {
        revertPosition();
        super.onClose();
    }

    void cancelWithoutSave() {
        revertPosition();
        if (minecraft != null) {
            minecraft.setScreenAndShow(null);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }

        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }

        OverlayWidgetKind hit = WidgetBounds.hitTest(event.x(), event.y());
        if (hit == null) {
            return false;
        }

        WidgetBounds bounds = boundsFor(hit);
        draggingWidget = hit;
        OverlayEditState.setDragging(true);
        dragOffsetX = event.x() - bounds.centerPixelX();
        dragOffsetY = event.y() - bounds.centerPixelY();
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (super.mouseDragged(event, dx, dy)) {
            return true;
        }

        if (draggingWidget == null || event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }

        var config = TpsOverlayConfigHandler.getConfig();
        float rawCenterX = OverlayPosition.centerFromPixelX(event.x() - dragOffsetX, width);
        float rawCenterY = OverlayPosition.centerFromPixelY(event.y() - dragOffsetY, height);

        WidgetBounds bounds = boundsFor(draggingWidget);
        if (bounds.valid()) {
            var snapped = OverlayEditSnapping.snap(
                rawCenterX,
                rawCenterY,
                bounds.contentWidth(),
                bounds.contentHeight(),
                bounds.screenWidth(),
                bounds.screenHeight()
            );
            OverlayWidgets.setCenter(config, draggingWidget, snapped.centerX(), snapped.centerY());
            OverlayEditState.setGuides(snapped.guideX(), snapped.guideY());
        } else {
            OverlayWidgets.setCenter(
                config,
                draggingWidget,
                OverlayPosition.clampCenterX(rawCenterX, 0, width),
                OverlayPosition.clampCenterY(rawCenterY, 0, height)
            );
            OverlayEditState.setGuides(false, false);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (super.mouseReleased(event)) {
            return true;
        }

        if (draggingWidget != null && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            draggingWidget = null;
            OverlayEditState.setDragging(false);
            return true;
        }
        return false;
    }

    @Override
    public void removed() {
        draggingWidget = null;
        OverlayEditState.setDragging(false);
        OverlayDragHandler.onEditScreenClosed();
        super.removed();
    }

    private void resetPosition() {
        var defaults = TpsOverlayConfigHandler.defaults();
        var config = TpsOverlayConfigHandler.getConfig();
        OverlayWidgets.reset(config, defaults, OverlayWidgetKind.TEXT);
        OverlayWidgets.reset(config, defaults, OverlayWidgetKind.GRAPH);
    }

    private void revertPosition() {
        var config = TpsOverlayConfigHandler.getConfig();
        config.overlayX = savedTextX;
        config.overlayY = savedTextY;
        config.graphCenterX = savedGraphX;
        config.graphCenterY = savedGraphY;
    }

    private void saveAndClose() {
        TpsOverlayConfigHandler.save();
        if (minecraft != null) {
            minecraft.setScreenAndShow(null);
        }
    }

    private static WidgetBounds boundsFor(OverlayWidgetKind kind) {
        return switch (kind) {
            case TEXT -> WidgetBounds.TEXT;
            case GRAPH -> WidgetBounds.GRAPH;
        };
    }
}
