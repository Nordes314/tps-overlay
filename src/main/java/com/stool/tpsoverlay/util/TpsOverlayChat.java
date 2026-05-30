package com.stool.tpsoverlay.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TpsOverlayChat {
    private TpsOverlayChat() {
    }

    public static MutableComponent prefix() {
        return Component.literal("[")
            .withStyle(ChatFormatting.DARK_BLUE)
            .append(Component.literal("TPSO").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .append(Component.literal("] ").withStyle(ChatFormatting.DARK_BLUE));
    }

    public static MutableComponent prefixed(Component message) {
        return prefix().append(message);
    }

    public static MutableComponent prefixedGray(Component message) {
        return prefix().append(message.copy().withStyle(ChatFormatting.GRAY));
    }
}
