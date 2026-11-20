package ru.crimson.client.visual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

            if (config.targetEsp
                    && attackDown
                    && !wasAttackDown
                    && client.screen == null) {

                if (client.crosshairPickEntity instanceof LivingEntity living
                        && living != client.player
                        && living.isAlive()
                        && !living.isInvisible()) {

                    target = living;
                    ticksLeft = 18;
                    animationTick = 0;
                }
            }

            wasAttackDown = attackDown;

            if (!config.targetEsp) {
                resetTarget();
                return;
            }

            if (target == null || !target.isAlive()
                    || target.isRemoved() || ticksLeft <= 0) {
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
        if (!config.targetEsp || target == null || !target.isAlive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        PoseStack matrices = context.matrixStack();
        MultiBufferSource consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        float tickDelta = context.tickCounter().getGameTimeDeltaPartialTick(true);
        Vec3 cam = context.camera().getPosition();

        double x = Mth.lerp(tickDelta, target.xOld, target.getX());
        double y = Mth.lerp(tickDelta, target.yOld, target.getY());
        double z = Mth.lerp(tickDelta, target.zOld, target.getZ());

        matrices.pushPose();
        matrices.translate(-cam.x, -cam.y, -cam.z);

        Matrix4f matrix = matrices.last().pose();
        VertexConsumer buffer = consumers.getBuffer(RenderType.lines());

        int color = config.targetEspColor;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = 0.75f + 0.2f * (float) Math.sin(animationTick * 0.3);

        float height = target.getBbHeight();
        float width = target.getBbWidth() * 0.55f;

        int pillars = 6;
        double time = animationTick * 0.15;
        for (int i = 0; i < pillars; i++) {
            double angle = (Math.PI * 2.0 * i / pillars) + time;
            double px = x + Math.cos(angle) * width;
            double pz = z + Math.sin(angle) * width;

            buffer.addVertex(matrix, (float) px, (float) y, (float) pz)
                    .setColor(r, g, b, a)
                    .setNormal(0, 1, 0);
            buffer.addVertex(matrix, (float) px, (float) (y + height), (float) pz)
                    .setColor(r, g, b, a * 0.3f)
                    .setNormal(0, 1, 0);
        }

        if (config.targetEspRing) {
            float progress = 1f - (ticksLeft / 18f);
            float radius = width * (0.8f + progress * 1.6f);
            float ringA = a * (1f - progress);

            int segs = 40;
            double prevX = x + radius;
            double prevZ = z;
            for (int i = 1; i <= segs; i++) {
                double angle = Math.PI * 2.0 * i / segs;
                double nx = x + Math.cos(angle) * radius;
                double nz = z + Math.sin(angle) * radius;

                buffer.addVertex(matrix, (float) prevX, (float) (y + 0.05), (float) prevZ)
                        .setColor(r, g, b, ringA)
                        .setNormal(0, 1, 0);
                buffer.addVertex(matrix, (float) nx, (float) (y + 0.05), (float) nz)
                        .setColor(r, g, b, ringA)
                        .setNormal(0, 1, 0);

                prevX = nx;
                prevZ = nz;
            }
        }

        matrices.popPose();
    }

    private static void spawnEffects(
            Minecraft client,
            CrimsonConfig config,
            LivingEntity entity
    ) {
        double centerX = entity.getX();
        double centerZ = entity.getZ();

        double radius = Math.max(0.45, entity.getBbWidth() * 0.75);
        double ringY = entity.getY() + 0.12;

        if (config.targetEspRing) {
            int count = 10;
            for (int i = 0; i < count; i++) {
                double angle = Math.PI * 2.0 * i / count + animationTick * 0.1;
                double x = centerX + Math.cos(angle) * radius;
                double z = centerZ + Math.sin(angle) * radius;

                client.level.addParticle(
                        ParticleTypes.END_ROD,
                        x, ringY, z,
                        0.0, 0.02, 0.0
                );
            }
        }

        if (config.targetEspParticles) {
            for (int i = 0; i < 4; i++) {
                double x = centerX + ThreadLocalRandom.current().nextDouble(-radius, radius);
                double y = entity.getY() + ThreadLocalRandom.current().nextDouble(0.2, entity.getBbHeight());
                double z = centerZ + ThreadLocalRandom.current().nextDouble(-radius, radius);

                client.level.addParticle(
                        ParticleTypes.CRIT,
                        x, y, z,
                        0.0, 0.03, 0.0
                );
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
