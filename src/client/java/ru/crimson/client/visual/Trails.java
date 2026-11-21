
package ru.crimson.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Trails {

    private static final Deque<TrailPoint> points = new ArrayDeque<>();

    private Trails() {}

    public static void init() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            CrimsonConfig cfg = CrimsonClientClient.config;

            if (client.player == null || client.level == null
                    || cfg == null || !cfg.trails) {
                points.clear();
                return;
            }

            Vec3 position = client.player.position().add(0, 0.1, 0);

            points.addLast(new TrailPoint(
                    position,
                    System.currentTimeMillis()
            ));

            int maxLength = Mth.clamp(cfg.trailsLength, 2, 200);

            while (points.size() > maxLength) {
                points.removeFirst();
            }
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(Trails::render);
    }

    private static void render(WorldRenderContext ctx) {

        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.trails || points.size() < 2) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) {
            return;
        }

        PoseStack matrices = ctx.matrixStack();

        MultiBufferSource.BufferSource buffers =
                mc.renderBuffers().bufferSource();

        VertexConsumer consumer =
                buffers.getBuffer(RenderType.lines());

        Vec3 camera = ctx.camera().getPosition();

        Matrix4f matrix = matrices.last().pose();

        int color = cfg.trailsColor;

        float red = ((color >> 16) & 255) / 255f;
        float green = ((color >> 8) & 255) / 255f;
        float blue = (color & 255) / 255f;

        float baseAlpha = ((color >>> 24) & 255) / 255f;

        TrailPoint previous = null;

        int index = 0;
        int total = points.size();

        for (TrailPoint point : points) {

            if (previous != null) {

                float alpha = baseAlpha;

                if (cfg.trailsFade) {
                    alpha *= (float) index / Math.max(1, total - 1);
                }

                float x1 = (float) (previous.position.x - camera.x);
                float y1 = (float) (previous.position.y - camera.y);
                float z1 = (float) (previous.position.z - camera.z);

                float x2 = (float) (point.position.x - camera.x);
                float y2 = (float) (point.position.y - camera.y);
                float z2 = (float) (point.position.z - camera.z);

                consumer.addVertex(matrix, x1, y1, z1)
                        .setColor(red, green, blue, alpha)
                        .setNormal(0, 1, 0);

                consumer.addVertex(matrix, x2, y2, z2)
                        .setColor(red, green, blue, alpha)
                        .setNormal(0, 1, 0);
            }

            previous = point;
            index++;
        }

        buffers.endBatch();
    }

    private record TrailPoint(Vec3 position, long time) {}
}
