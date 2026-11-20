package ru.crimson.client.visual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

public final class HitSound {

    private static boolean wasAttackDown;
    private static long lastPlay;

    private HitSound() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                wasAttackDown = false;
                return;
            }

            CrimsonConfig config = CrimsonClientClient.config;
            if (!config.hitSound) {
                wasAttackDown = client.options.keyAttack.isDown();
                return;
            }

            boolean attackDown = client.options.keyAttack.isDown();

            // Edge detect left click
            if (attackDown && !wasAttackDown && client.screen == null) {
                if (client.crosshairPickEntity instanceof LivingEntity living
                        && living != client.player
                        && living.isAlive()) {
                    play(client, config);
                }
            }

            wasAttackDown = attackDown;
        });
    }

    private static void play(Minecraft client, CrimsonConfig config) {
        long now = System.currentTimeMillis();
        if (now - lastPlay < 40) return; // anti-spam
        lastPlay = now;

        SoundEvent sound = switch (config.hitSoundType) {
            case 1 -> SoundEvents.PLAYER_ATTACK_CRIT;
            case 2 -> SoundEvents.PLAYER_ATTACK_STRONG;
            case 3 -> SoundEvents.PLAYER_ATTACK_KNOCKBACK;
            case 4 -> SoundEvents.ARROW_HIT_PLAYER;
            case 5 -> SoundEvents.EXPERIENCE_ORB_PICKUP;
            case 6 -> SoundEvents.NOTE_BLOCK_PLING.value();
            default -> SoundEvents.PLAYER_ATTACK_STRONG;
        };

        float volume = Math.max(0.05f, Math.min(1.0f, config.hitSoundVolume));
        float pitch = 0.95f + client.level.random.nextFloat() * 0.1f;

        client.getSoundManager().play(
                SimpleSoundInstance.forUI(sound, pitch, volume)
        );
    }
}
