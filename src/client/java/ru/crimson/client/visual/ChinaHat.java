
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

    private static void render(WorldRenderContext ctx) {
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.chinaHat) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) {
            return;
        }

        PoseStack matrices = ctx.matrices();
        MultiBufferSource consumers = ctx.consumers();

        if (matrices == null || consumers == null) {
            return;
        }

        Vec3 camera = ctx.gameRenderer()
                .getMainCamera().position();

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        matrices.pushPose();

        try {
            matrices.translate(
                    x - camera.x,
                    y - camera.y + mc.player.getBbHeight() + 0.15,
                    z - camera.z
            );

            VertexConsumer buffer =
                    consumers.getBuffer(RenderTypes.lines());

            Matrix4f matrix = matrices.last().pose();

            int color = cfg.chinaHatColor;

            if (cfg.chinaHatRainbow) {
                float hue = (System.currentTimeMillis() % 4000L)
                        / 4000f;

                color = java.awt.Color.HSBtoRGB(
                        hue, 0.85f, 1.0f
                ) | 0xAA000000;
            }

            float red = ((color >> 16) & 255) / 255f;
            float green = ((color >> 8) & 255) / 255f;
            float blue = (color & 255) / 255f;
            float alpha = ((color >>> 24) & 255) / 255f;

            float height = cfg.chinaHatHeight;
            float radius = cfg.chinaHatRadius;

            int segments = 32;

            for (int i = 0; i < segments; i++) {
                float a1 = (float) (i * Math.PI * 2 / segments);
                float a2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float x1 = Mth.cos(a1) * radius;
                float z1 = Mth.sin(a1) * radius;

                float x2 = Mth.cos(a2) * radius;
                float z2 = Mth.sin(a2) * radius;

                vertex(buffer, matrix,
                        0, height, 0,
                        red, green, blue, alpha);

                vertex(buffer, matrix,
                        x1, 0, z1,
                        red, green, blue, alpha);

                vertex(buffer, matrix,
                        x1, 0, z1,
                        red, green, blue, alpha * 0.65f);

                vertex(buffer, matrix,
                        x2, 0, z2,
                        red, green, blue, alpha * 0.65f);
            }
        } finally {
            matrices.popPose();
        }
    }

    private static void vertex(
            VertexConsumer buffer,
            Matrix4f matrix,
            float x, float y, float z,
            float r, float g, float b, float a
    ) {
        buffer.addVertex(matrix, x, y, z)
                .setLineWidth(1.0f)
                .setColor(r, g, b, a)
                .setNormal(0, 1, 0);
    }
}
