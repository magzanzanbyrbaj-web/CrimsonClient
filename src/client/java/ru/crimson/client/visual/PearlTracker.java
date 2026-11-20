
package ru.crimson.client.visual;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;
import ru.crimson.client.CrimsonClientClient;

import java.util.Locale;

public final class PearlTracker {
    private PearlTracker() {}

    public static void init() {
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player == null || mc.level == null
                    || mc.options.hideGui
                    || !CrimsonClientClient.config.pearlTracker) {
                return;
            }

            ThrownEnderpearl pearl = null;
            double nearest = Double.MAX_VALUE;

            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof ThrownEnderpearl)) continue;

                double distance = mc.player.distanceTo(entity);
                if (distance < nearest && distance < 128.0) {
                    nearest = distance;
                    pearl = (ThrownEnderpearl) entity;
                }
            }

            if (pearl == null) return;

            Vec3 position = pearl.position();
            Vec3 velocity = pearl.getDeltaMovement();

            double x = position.x;
            double y = position.y;
            double z = position.z;
            double vx = velocity.x;
            double vy = velocity.y;
            double vz = velocity.z;

            for (int tick = 0; tick < 100; tick++) {
                x += vx;
                y += vy;
                z += vz;

                vx *= 0.99;
                vy = (vy - 0.03) * 0.99;
                vz *= 0.99;

                if (y < mc.level.getMinY()) break;
            }

            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            String title = "Pearl Tracker";
            String distance = String.format(Locale.ROOT,
                    "Distance: %.1f blocks", nearest);
            String prediction = String.format(Locale.ROOT,
                    "Estimated: %.0f  %.0f  %.0f", x, y, z);

            int boxWidth = Math.max(mc.font.width(title),
                    Math.max(mc.font.width(distance),
                    mc.font.width(prediction))) + 14;

            int px = screenWidth / 2 + 12;
            int py = screenHeight / 2 + 32;

            graphics.fill(px, py, px + boxWidth, py + 48, 0xCC111521);
            graphics.fill(px, py, px + boxWidth, py + 2, 0xFFFF4055);

            graphics.drawString(mc.font, Component.literal(title),
                    px + 7, py + 6, 0xFFFF5365, true);
            graphics.drawString(mc.font, Component.literal(distance),
                    px + 7, py + 19, 0xFFF5F3F7, true);
            graphics.drawString(mc.font, Component.literal(prediction),
                    px + 7, py + 32, 0xFFB8B5C2, true);
        });
    }
}
