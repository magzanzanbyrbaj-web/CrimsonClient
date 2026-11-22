package ru.crimson.combat.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.client.hud.HudRenderer;
import ru.crimson.combat.util.TargetUtils;

@Environment(EnvType.CLIENT)
public class KillAura {
    private long lastAttack = 0L;

    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.killAura || mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }

        LivingEntity target = TargetUtils.findTarget(
                cfg.kaRange,
                cfg.kaFov,
                cfg.kaPlayers,
                cfg.kaMobs,
                cfg.kaThroughWalls,
                cfg.kaPriority
        );

        if (target == null) {
            return;
        }

        // Rotation (non-silent)
        if (!cfg.kaSilent) {
            float[] rotations = TargetUtils.getRotations(target);
            float smooth = Math.max(0.02f, Math.min(1.0f, cfg.kaSmoothness));
            mc.player.setYRot(TargetUtils.smoothAngle(mc.player.getYRot(), rotations[0], smooth));
            mc.player.setXRot(TargetUtils.smoothAngle(mc.player.getXRot(), rotations[1], smooth));
        }

        if (cfg.kaOnlyCriticals && !TargetUtils.isCriticalHitReady()) {
            return;
        }

        // CPS limit
        long delay = (long) (1000.0 / Math.max(1.0, Math.min(20.0, cfg.kaCps)));
        long now = System.currentTimeMillis();
        if (now - lastAttack < delay) {
            return;
        }

        // Attack cooldown check (1.21 style)
        if (mc.player.getAttackStrengthScale(0.5f) < 0.9f) {
            return;
        }

        // Attack
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);

        // HUD target
        try {
            HudRenderer.setTarget(target);
        } catch (Throwable ignored) {}

        lastAttack = now;
    }
}
