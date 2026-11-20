package ru.crimson.client.visual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class JumpCircle {

    private static final List<Circle> CIRCLES = new ArrayList<>();
    private static boolean wasOnGround = true;

    private JumpCircle() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                CIRCLES.clear();
                wasOnGround = true;
                return;
            }

            CrimsonConfig config = CrimsonClientClient.config;
            if (!config.jumpCircle) {
                CIRCLES.clear();
                wasOnGround = client.player.onGround();
                return;
            }

            LocalPlayer p = client.player;
            boolean onGround = p.onGround();

            // Detect jump (left ground)
            if (wasOnGround && !onGround && p.getDeltaMovement().y > 0.0) {
                CIRCLES.add(new Circle(
                        p.getX(),
                        p.getY(),
                        p.getZ(),
                        config.jumpCircleSize,
                        config.jumpCircleColor,
                        config.jumpCircleFade
                ));
            }

            wasOnGround = onGround;

            // Update lifetime
            Iterator<Circle> it = CIRCLES.iterator();
            while (it.hasNext()) {
                Circle c = it.next();
                c.age++;
                if (c.age > 18) {
                    it.remove();
                }
            }
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(JumpCircle::render);
    }

    private static void render(WorldRenderContext context) {
        if (CIRCLES.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        PoseStack matrices = context.matrixStack();
        MultiBufferSource consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3 cam = context.camera().getPosition();
        matrices.pushPose();
        matrices.translate(-cam.x, -cam.y, -cam.z);

        Matrix4f matrix = matrices.last().pose();
        VertexConsumer buffer = consumers.getBuffer(RenderType.lines());

        for (Circle c : CIRCLES) {
            float progress = c.age / 18f;
            float alpha = c.fade ? (1f - progress) : 0.85f;
            if (alpha <= 0.02f) continue;

            float radius = c.baseSize * (0.6f + progress * 1.4f);
            int color = c.color;
            float r = ((color >> 16) & 0xFF) / 255f;
            float g = ((color >> 8) & 0xFF) / 255f;
            float b = (color & 0xFF) / 255f;
            float a = alpha * 0.9f;

            int segments = 48;
            double prevX = c.x + radius;
            double prevZ = c.z;

            for (int i = 1; i <= segments; i++) {
                double angle = (Math.PI * 2.0 * i) / segments;
                double x = c.x + Math.cos(angle) * radius;
                double z = c.z + Math.sin(angle) * radius;

                buffer.addVertex(matrix, (float) prevX, (float) (c.y + 0.02), (float) prevZ)
                        .setColor(r, g, b, a)
                        .setNormal(0, 1, 0);
                buffer.addVertex(matrix, (float) x, (float) (c.y + 0.02), (float) z)
                        .setColor(r, g, b, a)
                        .setNormal(0, 1, 0);

                prevX = x;
                prevZ = z;
            }
        }

        matrices.popPose();
    }

    private static final class Circle {
        final double x, y, z;
        final float baseSize;
        final int color;
        final boolean fade;
        int age;

        Circle(double x, double y, double z, float size, int color, boolean fade) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.baseSize = size;
            this.color = color;
            this.fade = fade;
        }
    }
}
