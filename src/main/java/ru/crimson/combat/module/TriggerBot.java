package ru.crimson.combat.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import ru.crimson.combat.CrimsonCombat;
import ru.crimson.combat.util.TargetUtils;

public class TriggerBot {
    private long lastAttack = 0;

    public void onTick(MinecraftClient mc) {
        if (!CrimsonCombat.config.triggerBot || mc.player == null) return;

        long delay = 1000L / Math.max(1, CrimsonCombat.config.tbCps);
        if (System.currentTimeMillis() - lastAttack < delay) return;

        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        Entity entity = ((EntityHitResult) mc.crosshairTarget).getEntity();
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) return;

        if (mc.player.distanceTo(living) > CrimsonCombat.config.tbRange) return;

        boolean isPlayer = living instanceof net.minecraft.entity.player.PlayerEntity;
        if (isPlayer && !CrimsonCombat.config.tbPlayers) return;
        if (!isPlayer && !CrimsonCombat.config.tbMobs) return;

        if (!CrimsonCombat.config.tbThroughWalls && !TargetUtils.canSee(living)) return;

        if (mc.player.getAttackCooldownProgress(0.5f) >= 0.9f) {
            mc.interactionManager.attackEntity(mc.player, living);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastAttack = System.currentTimeMillis();
        }
    }
}
