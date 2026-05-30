package com.stool.tpsoverlay.client.hud;

import com.stool.tpsoverlay.config.TpsOverlayConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;

final class OverlayBackgroundRenderer {
    private OverlayBackgroundRenderer() {
    }

    static void draw(
        GuiGraphicsExtractor graphics,
        TpsOverlayConfig config,
        int x1,
        int y1,
        int x2,
        int y2,
        int alpha
    ) {
        int color = ARGB.color(alpha, config.backgroundColor & 0xFFFFFF);
        if (!config.backgroundRounded || config.backgroundRadius <= 0) {
            graphics.fill(x1, y1, x2, y2, color);
            return;
        }

        int radius = Math.min(config.backgroundRadius, Math.min((x2 - x1) / 2, (y2 - y1) / 2));
        if (radius <= 0) {
            graphics.fill(x1, y1, x2, y2, color);
            return;
        }

        graphics.fill(x1 + radius, y1, x2 - radius, y2, color);
        graphics.fill(x1, y1 + radius, x1 + radius, y2 - radius, color);
        graphics.fill(x2 - radius, y1 + radius, x2, y2 - radius, color);

        fillCorner(graphics, x1, y1, radius, color, true, true);
        fillCorner(graphics, x2 - radius, y1, radius, color, false, true);
        fillCorner(graphics, x1, y2 - radius, radius, color, true, false);
        fillCorner(graphics, x2 - radius, y2 - radius, radius, color, false, false);
    }

    private static void fillCorner(
        GuiGraphicsExtractor graphics,
        int originX,
        int originY,
        int radius,
        int color,
        boolean left,
        boolean top
    ) {
        int radiusSq = radius * radius;
        for (int dy = 0; dy < radius; dy++) {
            for (int dx = 0; dx < radius; dx++) {
                if (dx * dx + dy * dy > radiusSq) {
                    continue;
                }
                int px = left ? originX + (radius - 1 - dx) : originX + dx;
                int py = top ? originY + (radius - 1 - dy) : originY + dy;
                graphics.fill(px, py, px + 1, py + 1, color);
            }
        }
    }
}
