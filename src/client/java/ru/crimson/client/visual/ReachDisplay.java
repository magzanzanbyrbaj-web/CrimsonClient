
package ru.crimson.client.visual;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import ru.crimson.client.CrimsonClientClient;

import java.util.Locale;

public final class ReachDisplay {
    private ReachDisplay() {}

    public static void init() {
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player == null || mc.options.hideGui
                    || !CrimsonClientClient.config.reachDisplay) {
                return;
            }

            if (mc.crosshairPickEntity == null
                    || mc.crosshairPickEntity == mc.player) {
                return;
            }

            double distance = mc.player.distanceTo(mc.crosshairPickEntity);
            String text = String.format(Locale.ROOT, "Reach: %.2f blocks", distance);

            int width = mc.getWindow().getGuiScaledWidth();
            int x = width / 2 + 12;
            int y = mc.getWindow().getGuiScaledHeight() / 2 + 12;

            graphics.fill(x - 4, y - 3, x + mc.font.width(text) + 4,
                    y + 12, 0x990B101A);
            graphics.drawString(mc.font, Component.literal(text),
                    x, y, 0xFFFF5365, true);
        });
    }
}
