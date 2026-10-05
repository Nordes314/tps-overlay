package com.stool.tpsoverlay.client.hud;

import com.stool.tpsoverlay.config.TpsOverlayConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

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

        Screen screen = minecraft.gui.screen();
        if (editMode) {
            if (!(screen instanceof OverlayEditScreen)) {
                minecraft.setScreenAndShow(new OverlayEditScreen());
            }
        } else if (screen instanceof OverlayEditScreen editScreen) {
            editScreen.cancelWithoutSave();
        }
    }

    public static void onEditScreenClosed() {
        editMode = false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.gui.screen() == null && !minecraft.mouseHandler.isMouseGrabbed()) {
            minecraft.mouseHandler.grabMouse();
        }
    }

    public static boolean shouldCaptureMouse(Minecraft minecraft) {
        return editMode && minecraft.gui.screen() instanceof OverlayEditScreen && minecraft.player != null;
    }
}
