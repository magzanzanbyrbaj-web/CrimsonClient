
package ru.crimson.client.visual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import ru.crimson.client.CrimsonClientClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CriticalEffects {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final List<EffectParticle> PARTICLES = new ArrayList<>();

    private static boolean wasAttackDown;
    private static int tick;
    private static long lastEffect;

    private CriticalEffects() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                PARTICLES.clear();
                wasAttackDown = false;
                return;
            }

            tick++;

            boolean enabled = CrimsonClientClient.config.criticalEffects;
            boolean attackDown = client.options.keyAttack.isDown();

            if (enabled && attackDown && !wasAttackDown
                    && client.crosshairPickEntity != null
                    && client.crosshairPickEntity != client.player) {
                spawnEffect();
            }

            wasAttackDown = attackDown;

            updateParticles();
        });
    }

    private static void spawnEffect() {
        long now = System.currentTimeMillis();

        if (now - lastEffect < 120) return;
        lastEffect = now;

        int style = Math.floorMod(
                CrimsonClientClient.config.criticalStyle, 4
        );

        Vec3 pos = MC.player.position();

        switch (style) {
            case 1 -> spawnGhosts(pos);
            case 2 -> spawnSpiral(pos);
            case 3 -> spawnBurst(pos);
            default -> spawnStaircase(pos);
        }
    }

    private static void spawnStaircase(Vec3 pos) {
        for (int i = 0; i < 9; i++) {
            double x = (i % 3 - 1) * 0.32;
            double z = (i / 3 - 1) * 0.32;

            add(pos.x + x, pos.y + 0.15 + i * 0.13,
                    pos.z + z, 0, 0.025, 0, 18);
        }
    }

    private static void spawnGhosts(Vec3 pos) {
        for (int i = 0; i < 7; i++) {
            double angle = Math.PI * 2 * i / 7.0;
            double x = Math.cos(angle) * 0.65;
            double z = Math.sin(angle) * 0.65;

            add(pos.x + x, pos.y + 0.2,
                    pos.z + z, -x * 0.025, 0.045,
                    -z * 0.025, 30);
        }
    }

    private static void spawnSpiral(Vec3 pos) {
        for (int i = 0; i < 12; i++) {
            double angle = i * 0.65;
            double radius = 0.35 + i * 0.025;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            add(pos.x + x, pos.y + 0.12 + i * 0.07,
                    pos.z + z, 0, 0.02, 0, 25);
        }
    }

    private static void spawnBurst(Vec3 pos) {
        for (int i = 0; i < 16; i++) {
            double angle = Math.PI * 2 * i / 16.0;

            double vx = Math.cos(angle) * 0.09;
            double vz = Math.sin(angle) * 0.09;

            add(pos.x, pos.y + 0.8, pos.z,
                    vx, 0.035, vz, 20);
        }
    }

    private static void add(
            double x, double y, double z,
            double vx, double vy, double vz,
            int life
    ) {
        PARTICLES.add(new EffectParticle(
                x, y, z, vx, vy, vz, life
        ));
    }

    private static void updateParticles() {
        if (MC.level == null) return;

        Iterator<EffectParticle> iterator = PARTICLES.iterator();

        while (iterator.hasNext()) {
            EffectParticle p = iterator.next();

            p.x += p.vx;
            p.y += p.vy;
            p.z += p.vz;

            p.vx *= 0.94;
            p.vy *= 0.94;
            p.vz *= 0.94;

            p.age++;

            if (p.age >= p.life) {
                iterator.remove();
                continue;
            }

            if (p.age % 3 == 0) {
                MC.level.addParticle(
                        ParticleTypes.CRIT,
                        p.x, p.y, p.z,
                        0, 0.015, 0
                );
            }
        }

        if (PARTICLES.size() > 160) {
            PARTICLES.subList(0, PARTICLES.size() - 160).clear();
        }
    }

    private static final class EffectParticle {
        private double x, y, z;
        private double vx, vy, vz;
        private final int life;
        private int age;

        private EffectParticle(
                double x, double y, double z,
                double vx, double vy, double vz,
                int life
        ) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.life = life;
        }
    }
}
