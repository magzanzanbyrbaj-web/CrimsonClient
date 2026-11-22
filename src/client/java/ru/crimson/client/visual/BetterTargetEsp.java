
package ru.crimson.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

public final class BetterTargetEsp {

    private static LivingEntity target;
    private static float animation;

    private BetterTargetEsp() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CrimsonConfig cfg = CrimsonClientClient.config;

            if (client.player == null || cfg == null
                    || !cfg.targetEsp) {
                target = null;
                animation = 0;
                return;
            }

            LivingEntity lastTarget =
                    client.player.getLastHurtMob();

            if (lastTarget != null
                    && !lastTarget.isRemoved()
                    && !lastTarget.isDeadOrDying()) {
                target = lastTarget;
            }

            if (target != null
                    && (target.isRemoved()
                    || target.isDeadOrDying())) {
                target = null;
            }

            animation = Mth.clamp(
                    animation + (target != null ? 0.12f : -0.12f),
                    0f,
                    1f
            );
        });

        WorldRenderEvents.BEFORE_TRANSLUCENT.register(
                BetterTargetEsp::render
        );
    }

    private static void render(WorldRenderContext ctx) {
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.targetEsp
                || target == null || animation <= 0.01f) {
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

        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();

        matrices.pushPose();

        try {
            matrices.translate(
                    x - camera.x,
                    y - camera.y,
                    z - camera.z
            );

            float rotation = (System.currentTimeMillis() % 4000L)
                    / 4000f * 360f;

            matrices.mulPose(
                    com.mojang.math.Axis.YP.rotationDegrees(rotation)
            );

            Matrix4f matrix = matrices.last().pose();

            VertexConsumer buffer =
                    consumers.getBuffer(RenderTypes.lines());

            float height = target.getBbHeight();
            float width = target.getBbWidth()
                    * 0.7f * cfg.targetEspScale;

            int color = cfg.targetEspColor;

            float red = ((color >> 16) & 255) / 255f;
            float green = ((color >> 8) & 255) / 255f;
            float blue = (color & 255) / 255f;
            float alpha = ((color >>> 24) & 255)
                    / 255f * animation;

            int segments = 32;

            for (int i = 0; i < segments; i++) {
                float a1 = (float) (i * Math.PI * 2 / segments);
                float a2 = (float) ((i + 1) * Math.PI * 2 / segments);

                float x1 = Mth.cos(a1) * width;
                float z1 = Mth.sin(a1) * width;

                float x2 = Mth.cos(a2) * width;
                float z2 = Mth.sin(a2) * width;

                vertex(buffer, matrix,
                        x1, height * 0.35f, z1,
                        red, green, blue, alpha);

                vertex(buffer, matrix,
                        x2, height * 0.35f, z2,
                        red, green, blue, alpha);

                vertex(buffer, matrix,
                        x1, -height * 0.35f, z1,
                        red, green, blue, alpha * 0.6f);

                vertex(buffer, matrix,
                        x2, -height * 0.35f, z2,
                        red, green, blue, alpha * 0.6f);
            }

            for (int i = 0; i < 4; i++) {
                float angle = (float) (i * Math.PI / 2);

                float lx = Mth.cos(angle) * width;
                float lz = Mth.sin(angle) * width;

                vertex(buffer, matrix,
                        lx, -height * 0.35f, lz,
                        red, green, blue, alpha);

                vertex(buffer, matrix,
                        lx, height * 0.35f, lz,
                        red, green, blue, alpha);
            }

        } finally {
            matrices.popPose();
        }
    }

    private static void vertex(
            VertexConsumer buffer,
            Matrix4f matrix,
            float x,
            float y,
            float z,
            float r,
            float g,
            float b,
            float a
    ) {
        buffer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setLineWidth(1.0f)
                .setNormal(0, 1, 0);
    }
}
