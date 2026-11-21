package ru.crimson.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

/**
 * ChinaHat - красивая конусная шляпа над головой игрока.
 * Работает в world render.
 */
public final class ChinaHat {

    private ChinaHat() {}

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(ChinaHat::render);
    }

    private static void render(WorldRenderContext ctx) {
        CrimsonConfig cfg = CrimsonClientClient.config;
        if (cfg == null || !cfg.chinaHat) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack matrices = ctx.matrixStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lines()); // или debug lines / custom

        float partial = ctx.tickCounter().getGameTimeDeltaPartialTick(true);

        // Рисуем только на себе (можно расширить на других)
        renderHat(matrices, consumer, mc.player, partial, cfg);

        buffers.endBatch();
    }

    private static void renderHat(PoseStack matrices, VertexConsumer consumer, Player player,
                                  float partial, CrimsonConfig cfg) {
        double x = Mth.lerp(partial, player.xo, player.getX()) - Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().x;
        double y = Mth.lerp(partial, player.yo, player.getY()) - Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().y + player.getBbHeight() + 0.15;
        double z = Mth.lerp(partial, player.zo, player.getZ()) - Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().z;

        matrices.pushPose();
        matrices.translate(x, y, z);

        Matrix4f matrix = matrices.last().pose();

        int color = cfg.chinaHatColor;
        if (cfg.chinaHatRainbow) {
            float hue = (System.currentTimeMillis() % 3000) / 3000f;
            color = java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f) | 0xAA000000;
        }

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        float height = cfg.chinaHatHeight;
        float radius = cfg.chinaHatRadius;
        int segments = 24;

        // Конус
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = (float) Math.cos(angle1) * radius;
            float z1 = (float) Math.sin(angle1) * radius;
            float x2 = (float) Math.cos(angle2) * radius;
            float z2 = (float) Math.sin(angle2) * radius;

            // боковые линии
            vertex(consumer, matrix, 0, height, 0, r, g, b, a);
            vertex(consumer, matrix, x1, 0, z1, r, g, b, a);

            // основание
            vertex(consumer, matrix, x1, 0, z1, r, g, b, a * 0.6f);
            vertex(consumer, matrix, x2, 0, z2, r, g, b, a * 0.6f);
        }

        matrices.popPose();
    }

    private static void vertex(VertexConsumer c, Matrix4f m, float x, float y, float z,
                               float r, float g, float b, float a) {
        c.addVertex(m, x, y, z).setColor(r, g, b, a).setNormal(0, 1, 0);
    }
}
