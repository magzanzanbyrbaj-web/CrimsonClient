
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
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Trails {
    private static final Deque<Vec3> POINTS = new ArrayDeque<>();

    private Trails() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CrimsonConfig cfg = CrimsonClientClient.config;

            if (client.player == null || client.level == null
                    || cfg == null || !cfg.trails) {
                POINTS.clear();
                return;
            }

            POINTS.addLast(client.player.position().add(0, 0.1, 0));

            int maxLength = Mth.clamp(cfg.trailsLength, 2, 200);

            while (POINTS.size() > maxLength) {
                POINTS.removeFirst();
            }
        });

        WorldRenderEvents.BEFORE_TRANSLUCENT.register(Trails::render);
    }

    private static void render(WorldRenderContext context) {
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.trails || POINTS.size() < 2) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) return;

        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();

        if (matrices == null || consumers == null) return;

        Vec3 camera = context.gameRenderer()
                .getMainCamera().position();

        VertexConsumer buffer =
                consumers.getBuffer(RenderTypes.lines());

        Matrix4f matrix = matrices.last().pose();

        int color = cfg.trailsColor;

        float red = ((color >> 16) & 255) / 255f;
        float green = ((color >> 8) & 255) / 255f;
        float blue = (color & 255) / 255f;
        float alpha = ((color >>> 24) & 255) / 255f;

        int total = POINTS.size();
        int index = 0;

        Vec3 previous = null;

        for (Vec3 point : POINTS) {
            if (previous != null) {
                float fade = cfg.trailsFade
                        ? (float) index / Math.max(1, total - 1)
                        : 1.0f;

                float currentAlpha = alpha * fade;

                buffer.addVertex(
                        matrix,
                        (float) (previous.x - camera.x),
                        (float) (previous.y - camera.y),
                        (float) (previous.z - camera.z)
                )
                        .setLineWidth(3.0f)
                        .setColor(red, green, blue, currentAlpha)
                        .setNormal(0, 1, 0);

                buffer.addVertex(
                        matrix,
                        (float) (point.x - camera.x),
                        (float) (point.y - camera.y),
                        (float) (point.z - camera.z)
                )
                        .setLineWidth(3.0f)
                        .setColor(red, green, blue, currentAlpha)
                        .setNormal(0, 1, 0);
            }

            previous = point;
            index++;
        }
    }
}
