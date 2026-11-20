
package ru.crimson.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.concurrent.ThreadLocalRandom;

public final class TargetEsp {
    private static LivingEntity target;
    private static int ticksLeft;
    private static int animationTick;
    private static boolean wasAttackDown;

    private TargetEsp() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                reset();
                return;
            }

            CrimsonConfig config = CrimsonClientClient.config;
            boolean attackDown = client.options.keyAttack.isDown();

            if (config.targetEsp && attackDown && !wasAttackDown
                    && client.screen == null
                    && client.crosshairPickEntity instanceof LivingEntity living
                    && living != client.player && living.isAlive()
                    && !living.isInvisible()) {
                target = living;
                ticksLeft = 18;
                animationTick = 0;
            }

            wasAttackDown = attackDown;

            if (!config.targetEsp || target == null
                    || !target.isAlive() || target.isRemoved()
                    || ticksLeft <= 0) {
                resetTarget();
                return;
            }

            if (animationTick++ % 2 == 0) {
                spawnEffects(client, config, target);
            }

            ticksLeft--;
        });

        WorldRenderEvents.AFTER_ENTITIES.register(TargetEsp::renderWorld);
    }

    private static void renderWorld(WorldRenderContext context) {
        CrimsonConfig config = CrimsonClientClient.config;
        if (config == null || !config.targetEsp || target == null || !target.isAlive()) return;

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

            int color = config.targetEspColor;
            float r = ((color >> 16) & 255) / 255f;
            float g = ((color >> 8) & 255) / 255f;
            float b = (color & 255) / 255f;
            float a = 0.75f + 0.2f * (float) Math.sin(animationTick * 0.3);

            float height = target.getBbHeight();
            float width = target.getBbWidth() * 0.55f;

            for (int i = 0; i < 6; i++) {
                double angle = Math.PI * 2.0 * i / 6 + animationTick * 0.15;
                float px = (float) (target.getX() + Math.cos(angle) * width);
                float pz = (float) (target.getZ() + Math.sin(angle) * width);

                vertex(buffer, matrix, px, (float) target.getY(), pz, r, g, b, a);
                vertex(buffer, matrix, px, (float) (target.getY() + height), pz, r, g, b, a * 0.3f);
            }

            if (config.targetEspRing) {
                float progress = 1f - ticksLeft / 18f;
                float radius = width * (0.8f + progress * 1.6f);
                float alpha = a * (1f - progress);

                for (int i = 0; i < 40; i++) {
                    double a1 = Math.PI * 2.0 * i / 40;
                    double a2 = Math.PI * 2.0 * (i + 1) / 40;

                    vertex(buffer, matrix,
                            (float) (target.getX() + Math.cos(a1) * radius),
                            (float) (target.getY() + 0.05),
                            (float) (target.getZ() + Math.sin(a1) * radius),
                            r, g, b, alpha);

                    vertex(buffer, matrix,
                            (float) (target.getX() + Math.cos(a2) * radius),
                            (float) (target.getY() + 0.05),
                            (float) (target.getZ() + Math.sin(a2) * radius),
                            r, g, b, alpha);
                }
            }
        } finally {
            matrices.popPose();
        }
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix,
                               float x, float y, float z,
                               float r, float g, float b, float a) {
        buffer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setNormal(0, 1, 0);
        buffer.setLineWidth(1.5f);
    }

    private static void spawnEffects(Minecraft client,
                                     CrimsonConfig config,
                                     LivingEntity entity) {
        double radius = Math.max(0.45, entity.getBbWidth() * 0.75);

        if (config.targetEspRing) {
            for (int i = 0; i < 10; i++) {
                double angle = Math.PI * 2.0 * i / 10 + animationTick * 0.1;
                client.level.addParticle(
                        ParticleTypes.END_ROD,
                        entity.getX() + Math.cos(angle) * radius,
                        entity.getY() + 0.12,
                        entity.getZ() + Math.sin(angle) * radius,
                        0, 0.02, 0
                );
            }
        }

        if (config.targetEspParticles) {
            for (int i = 0; i < 4; i++) {
                double px = entity.getX()
                        + ThreadLocalRandom.current().nextDouble(-radius, radius);
                double py = entity.getY()
                        + ThreadLocalRandom.current().nextDouble(0.2, entity.getBbHeight());
                double pz = entity.getZ()
                        + ThreadLocalRandom.current().nextDouble(-radius, radius);

                client.level.addParticle(ParticleTypes.CRIT, px, py, pz, 0, 0.03, 0);
            }
        }
    }

    private static void resetTarget() {
        target = null;
        ticksLeft = 0;
        animationTick = 0;
    }

    private static void reset() {
        resetTarget();
        wasAttackDown = false;
    }
}
