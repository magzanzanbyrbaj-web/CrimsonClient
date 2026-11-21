
package ru.crimson.client.visual;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import ru.crimson.client.CrimsonClientClient;

public final class ReachDisplay {

    private static final double REACH_DISTANCE = 3.0;

    private ReachDisplay() {}

    public static void init() {
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player == null || mc.options.hideGui
                    || CrimsonClientClient.config == null
                    || !CrimsonClientClient.config.reachDisplay) {
                return;
            }

            Entity target = mc.crosshairPickEntity;

            if (target == null || target == mc.player) {
                return;
            }

            double distance = mc.player.distanceTo(target);
            boolean inReach = distance <= REACH_DISTANCE;

            int color = inReach
                    ? 0xFF45E68A
                    : 0xFFFF465C;

            int centerX = mc.getWindow().getGuiScaledWidth() / 2;
            int centerY = mc.getWindow().getGuiScaledHeight() / 2 + 18;

            drawCircle(graphics, centerX, centerY, 7, color);

            String text = String.format(
                    java.util.Locale.ROOT,
                    "%.1f",
                    distance
            );

            int textWidth = mc.font.width(text);

            graphics.drawString(
                    mc.font,
                    text,
                    centerX - textWidth / 2,
                    centerY + 11,
                    0xFFFFFFFF,
                    true
            );
        });
    }

    private static void drawCircle(
            net.minecraft.client.gui.GuiGraphics graphics,
            int centerX,
            int centerY,
            int radius,
            int color
    ) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                int distance = x * x + y * y;

                if (distance <= radius * radius
                        && distance >= (radius - 2) * (radius - 2)) {
                    graphics.fill(
                            centerX + x,
                            centerY + y,
                            centerX + x + 1,
                            centerY + y + 1,
                            color
                    );
                }
            }
        }
    }
}
