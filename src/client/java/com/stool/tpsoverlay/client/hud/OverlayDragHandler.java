package com.stool.tpsoverlay.client.hud;

import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;
import net.minecraft.client.Minecraft;

public final class OverlayDragHandler {
    private static boolean editMode;

    private OverlayDragHandler() {
    }

    public static boolean isEditMode() {
        return editMode;
    }

    public static void toggleEditMode() {
        Minecraft minecraft = Minecraft.getInstance();
        editMode = !editMode;

        if (minecraft.player == null) {
            return;
        }

        if (editMode) {
            if (!(minecraft.screen instanceof OverlayEditScreen)) {
                minecraft.setScreen(new OverlayEditScreen());
            }
        } else if (minecraft.screen instanceof OverlayEditScreen editScreen) {
            editScreen.cancelWithoutSave();
        }
    }

    public static void onEditScreenClosed() {
        editMode = false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.screen == null && !minecraft.mouseHandler.isMouseGrabbed()) {
            minecraft.mouseHandler.grabMouse();
        }
    }

    public static boolean shouldCaptureMouse(Minecraft minecraft) {
        return editMode && minecraft.screen instanceof OverlayEditScreen && minecraft.player != null;
    }
}
