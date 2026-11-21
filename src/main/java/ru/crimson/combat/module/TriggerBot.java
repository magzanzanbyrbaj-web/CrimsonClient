package ru.crimson.combat.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.combat.util.TargetUtils;

public class TriggerBot {

    private long lastAttack = 0;

    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.triggerBot || mc.player == null || mc.level == null) return;

        long delay = 1000L / Math.max(1, cfg.tbCps);
        if (System.currentTimeMillis() - lastAttack < delay) return;

        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.ENTITY) return;

        Entity entity = ((EntityHitResult) mc.hitResult).getEntity();
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) return;

        if (mc.player.distanceTo(living) > cfg.tbRange) return;

        boolean isPlayer = living instanceof Player;
        if (isPlayer && !cfg.tbPlayers) return;
        if (!isPlayer && !cfg.tbMobs) return;

        if (!cfg.tbThroughWalls && !TargetUtils.canSee(living)) return;

        if (mc.player.getAttackStrengthScale(0.5f) >= 0.9f) {
            mc.gameMode.attack(mc.player, living);
            mc.player.swing(InteractionHand.MAIN_HAND);
            lastAttack = System.currentTimeMillis();
        }
    }
}
