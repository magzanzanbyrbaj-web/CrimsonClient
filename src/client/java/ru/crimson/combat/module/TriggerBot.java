package ru.crimson.combat.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.client.hud.HudRenderer;
import ru.crimson.combat.util.TargetUtils;

@Environment(EnvType.CLIENT)
public class TriggerBot {
    private long lastAttack = 0L;

    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.triggerBot || mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }

        // Only when looking at an entity
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }

        Entity entity = ((EntityHitResult) mc.hitResult).getEntity();
        if (!(entity instanceof LivingEntity target) || !target.isAlive()) {
            return;
        }

        boolean isPlayer = target instanceof Player;
        if (isPlayer && !cfg.tbPlayers) return;
        if (!isPlayer && !cfg.tbMobs) return;

        if ((double) mc.player.distanceTo(target) > cfg.tbRange) {
            return;
        }

        if (!cfg.tbThroughWalls && !TargetUtils.canSee(target)) {
            return;
        }

        if (cfg.tbOnlyCriticals && !TargetUtils.isCriticalHitReady()) {
            return;
        }

        long delay = (long) (1000.0 / Math.max(1.0, Math.min(20.0, cfg.tbCps)));
        long now = System.currentTimeMillis();
        if (now - lastAttack < delay) {
            return;
        }

        if (mc.player.getAttackStrengthScale(0.5f) < 0.9f) {
            return;
        }

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);

        try {
            HudRenderer.setTarget(target);
        } catch (Throwable ignored) {}

        lastAttack = now;
    }
}
