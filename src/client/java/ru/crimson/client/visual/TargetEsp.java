
package ru.crimson.client.visual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
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

            // Detect a new manual left-click.
            if (config.targetEsp
                    && attackDown
                    && !wasAttackDown
                    && client.screen == null) {

                if (client.crosshairPickEntity instanceof LivingEntity living
                        && living != client.player
                        && living.isAlive()
                        && !living.isInvisible()) {

                    target = living;
                    ticksLeft = 14;
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

            // Update the visual effect every second tick.
            if (animationTick++ % 2 == 0) {
                spawnEffects(client, config, target);
            }

            ticksLeft--;
        });
    }

    private static void spawnEffects(
            Minecraft client,
            CrimsonConfig config,
            LivingEntity entity
    ) {
        double centerX = entity.getX();
        double centerZ = entity.getZ();

        double radius = Math.max(
                0.45,
                entity.getBbWidth() * 0.75
        );

        double ringY = entity.getY() + 0.12;

        // Circular particles around the target's feet.
        if (config.targetEspRing) {
            int count = 12;

            for (int i = 0; i < count; i++) {
                double angle = Math.PI * 2.0 * i / count;

                double x = centerX + Math.cos(angle) * radius;
                double z = centerZ + Math.sin(angle) * radius;

                client.level.addParticle(
                        ParticleTypes.END_ROD,
                        x,
                        ringY,
                        z,
                        0.0,
                        0.025,
                        0.0
                );
            }
        }

        // Additional sparks around the target.
        if (config.targetEspParticles) {
            for (int i = 0; i < 5; i++) {
                double x = centerX
                        + ThreadLocalRandom.current().nextDouble(-radius, radius);

                double y = entity.getY()
                        + ThreadLocalRandom.current().nextDouble(
                                0.25, entity.getBbHeight()
                        );

                double z = centerZ
                        + ThreadLocalRandom.current().nextDouble(-radius, radius);

                client.level.addParticle(
                        ParticleTypes.CRIT,
                        x,
                        y,
                        z,
                        0.0,
                        0.035,
                        0.0
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
