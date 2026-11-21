
package ru.crimson.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
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
            if (client.player == null || client.level == null
                    || CrimsonClientClient.config == null) {
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

            boolean grounded = client.player.onGround();

            if (wasOnGround && !grounded
                    && client.player.getDeltaMovement().y > 0) {
                CIRCLES.add(new Circle(
                        client.player.getX(),
                        client.player.getY() + 0.03,
                        client.player.getZ(),
                        config.jumpCircleSize,
                        config.jumpCircleColor,
                        config.jumpCircleFade
                ));
            }

            wasOnGround = grounded;

            Iterator<Circle> iterator = CIRCLES.iterator();
            while (iterator.hasNext()) {
                Circle circle = iterator.next();
                circle.age++;
                if (circle.age >= 24) iterator.remove();
            }
        });

        WorldRenderEvents.BEFORE_TRANSLUCENT.register(JumpCircle::render);
    }

    private static void render(WorldRenderContext context) {
        if (CIRCLES.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();

        if (matrices == null || consumers == null) return;

        Vec3 camera = context.gameRenderer()
                .getMainCamera().position();

        VertexConsumer buffer =
                consumers.getBuffer(RenderTypes.lines());

        matrices.pushPose();

        try {
            matrices.translate(-camera.x, -camera.y, -camera.z);

            Matrix4f matrix = matrices.last().pose();

            for (Circle circle : CIRCLES) {
                float progress = circle.age / 24.0f;
                float radius = circle.baseSize * (0.65f + progress * 1.8f);
                float alpha = circle.fade ? 1.0f - progress : 0.95f;

                if (alpha <= 0.02f) continue;

                float red = ((circle.color >> 16) & 255) / 255f;
                float green = ((circle.color >> 8) & 255) / 255f;
                float blue = (circle.color & 255) / 255f;

                for (int i = 0; i < 64; i++) {
                    double a1 = Math.PI * 2.0 * i / 64;
                    double a2 = Math.PI * 2.0 * (i + 1) / 64;

                    float x1 = (float) (circle.x + Math.cos(a1) * radius);
                    float z1 = (float) (circle.z + Math.sin(a1) * radius);
                    float x2 = (float) (circle.x + Math.cos(a2) * radius);
                    float z2 = (float) (circle.z + Math.sin(a2) * radius);

                    buffer.addVertex(matrix, x1, (float) circle.y, z1)
                            .setLineWidth(3.0f)
                            .setColor(red, green, blue, alpha)
                            .setNormal(0, 1, 0);

                    buffer.addVertex(matrix, x2, (float) circle.y, z2)
                            .setLineWidth(3.0f)
                            .setColor(red, green, blue, alpha)
                            .setNormal(0, 1, 0);
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
