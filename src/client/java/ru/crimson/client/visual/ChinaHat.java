
package ru.crimson.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

public final class ChinaHat {
    private ChinaHat() {}

    public static void init() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(ChinaHat::render);
    }

    private static void render(WorldRenderContext context) {
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.chinaHat) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) return;

        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();

        if (matrices == null || consumers == null) return;

        Vec3 camera = context.gameRenderer()
                .getMainCamera().position();

        matrices.pushPose();

        try {
            matrices.translate(
                    mc.player.getX() - camera.x,
                    mc.player.getY() - camera.y
                            + mc.player.getBbHeight() + 0.15,
                    mc.player.getZ() - camera.z
            );

            Matrix4f matrix = matrices.last().pose();

            int color = cfg.chinaHatColor;

            if (cfg.chinaHatRainbow) {
                float hue = (System.currentTimeMillis() % 5000L)
                        / 5000.0f;

                color = java.awt.Color.HSBtoRGB(
                        hue, 0.85f, 1.0f
                );
            }

            float red = ((color >> 16) & 255) / 255f;
            float green = ((color >> 8) & 255) / 255f;
            float blue = (color & 255) / 255f;

            float height = cfg.chinaHatHeight;
            float radius = cfg.chinaHatRadius;

            int segments = 48;

            VertexConsumer lines =
                    consumers.getBuffer(RenderTypes.lines());

            for (int i = 0; i < segments; i++) {
                float a1 = (float) (i * Math.PI * 2 / segments);
                float a2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float x1 = Mth.cos(a1) * radius;
                float z1 = Mth.sin(a1) * radius;
                float x2 = Mth.cos(a2) * radius;
                float z2 = Mth.sin(a2) * radius;

                lines.addVertex(matrix, 0, height, 0)
                        .setLineWidth(1.5f)
                        .setColor(red, green, blue, 0.65f)
                        .setNormal(0, 1, 0);

                lines.addVertex(matrix, x1, 0, z1)
                        .setLineWidth(1.5f)
                        .setColor(red, green, blue, 0.65f)
                        .setNormal(0, 1, 0);

                lines.addVertex(matrix, x1, 0, z1)
                        .setLineWidth(1.5f)
                        .setColor(red, green, blue, 0.8f)
                        .setNormal(0, 1, 0);

                lines.addVertex(matrix, x2, 0, z2)
                        .setLineWidth(1.5f)
                        .setColor(red, green, blue, 0.8f)
                        .setNormal(0, 1, 0);
            }
        } finally {
            matrices.popPose();
        }
    }
}
