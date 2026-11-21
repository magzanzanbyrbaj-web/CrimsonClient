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

/**
 * Trails - след за игроком (линия позиций).
 */
public final class Trails {

    private static final Deque<TrailPoint> points = new ArrayDeque<>();

    private Trails() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                points.clear();
                return;
            }
            CrimsonConfig cfg = CrimsonClientClient.config;
            if (cfg == null || !cfg.trails) {
                points.clear();
                return;
            }

            Vec3 pos = client.player.position().add(0, 0.1, 0);
            points.addLast(new TrailPoint(pos, System.currentTimeMillis()));

            while (points.size() > cfg.trailsLength) {
                points.removeFirst();
            }
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(Trails::render);
    }

    private static void render(WorldRenderContext ctx) {
        CrimsonConfig cfg = CrimsonClientClient.config;
        if (cfg == null || !cfg.trails || points.size() < 2) return;

        Minecraft mc = Minecraft.getInstance();
        PoseStack matrices = ctx.matrixStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lines());

        Vec3 cam = ctx.camera().getPosition();
        Matrix4f matrix = matrices.last().pose();

        int color = cfg.trailsColor;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float baseA = ((color >> 24) & 0xFF) / 255f;

        TrailPoint prev = null;
        int i = 0;
        int total = points.size();

        for (TrailPoint p : points) {
            if (prev != null) {
                float alpha = baseA;
                if (cfg.trailsFade) {
                    alpha *= (float) i / total;
                }

                float x1 = (float) (prev.pos.x - cam.x);
                float y1 = (float) (prev.pos.y - cam.y);
                float z1 = (float) (prev.pos.z - cam.z);
                float x2 = (float) (p.pos.x - cam.x);
                float y2 = (float) (p.pos.y - cam.y);
                float z2 = (float) (p.pos.z - cam.z);

                consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, alpha).setNormal(0, 1, 0);
                consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, alpha).setNormal(0, 1, 0);
            }
            prev = p;
            i++;
        }

        buffers.endBatch();
    }

    private record TrailPoint(Vec3 pos, long time) {}
          }
