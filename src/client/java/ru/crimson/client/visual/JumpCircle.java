
package ru.crimson.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
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
            LocalPlayer player = client.player;

            if (!config.jumpCircle) {
                CIRCLES.clear();
                wasOnGround = player.onGround();
                return;
            }

            boolean onGround = player.onGround();

            if (wasOnGround && !onGround && player.getDeltaMovement().y > 0) {
                CIRCLES.add(new Circle(
                        player.getX(), player.getY(), player.getZ(),
                        config.jumpCircleSize,
                        config.jumpCircleColor,
                        config.jumpCircleFade
                ));
            }

            wasOnGround = onGround;

            Iterator<Circle> iterator = CIRCLES.iterator();
            while (iterator.hasNext()) {
                Circle circle = iterator.next();
                circle.age++;
                if (circle.age > 18) iterator.remove();
            }
        });

        WorldRenderEvents.BEFORE_TRANSLUCENT.register(JumpCircle::render);
    }

    private static void render(WorldRenderContext context) {
        if (CIRCLES.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3 cam = context.gameRenderer().getMainCamera().position();

        matrices.pushPose();
        try {
            matrices.translate(-cam.x, -cam.y, -cam.z);

            Matrix4f matrix = matrices.last().pose();
            VertexConsumer buffer = consumers.getBuffer(RenderTypes.lines());

            for (Circle circle : CIRCLES) {
                float progress = circle.age / 18f;
                float alpha = circle.fade ? 1f - progress : 0.85f;
                if (alpha <= 0.02f) continue;

                float radius = circle.baseSize * (0.6f + progress * 1.4f);

                int color = circle.color;
                float r = ((color >> 16) & 255) / 255f;
                float g = ((color >> 8) & 255) / 255f;
                float b = (color & 255) / 255f;

                for (int i = 0; i < 48; i++) {
                    double angle1 = Math.PI * 2.0 * i / 48;
                    double angle2 = Math.PI * 2.0 * (i + 1) / 48;

                    float x1 = (float) (circle.x + Math.cos(angle1) * radius);
                    float z1 = (float) (circle.z + Math.sin(angle1) * radius);
                    float x2 = (float) (circle.x + Math.cos(angle2) * radius);
                    float z2 = (float) (circle.z + Math.sin(angle2) * radius);

                    buffer.addVertex(matrix, x1, (float) (circle.y + 0.02), z1)
                            .setColor(r, g, b, alpha)
                            .setNormal(0, 1, 0);
                    buffer.setLineWidth(1.5f);

                    buffer.addVertex(matrix, x2, (float) (circle.y + 0.02), z2)
                            .setColor(r, g, b, alpha)
                            .setNormal(0, 1, 0);
                    buffer.setLineWidth(1.5f);
                }
            }
        } finally {
            matrices.popPose();
        }
    }

    private static final class Circle {
        final double x, y, z;
        final float baseSize;
        final int color;
        final boolean fade;
        int age;

        Circle(double x, double y, double z,
               float size, int color, boolean fade) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.baseSize = size;
            this.color = color;
            this.fade = fade;
        }
    }
}
