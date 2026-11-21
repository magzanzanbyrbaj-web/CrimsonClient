package ru.crimson.combat.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import ru.crimson.combat.CrimsonCombat;
import ru.crimson.combat.util.TargetUtils;

public class KillAura {
    private long lastAttack = 0;

    public void onTick(MinecraftClient mc) {
        if (!CrimsonCombat.config.killAura || mc.player == null) return;

        long delay = 1000L / Math.max(1, CrimsonCombat.config.kaCps);
        if (System.currentTimeMillis() - lastAttack < delay) return;

        LivingEntity target = TargetUtils.findTarget(
                CrimsonCombat.config.kaRange,
                CrimsonCombat.config.kaFov,
                CrimsonCombat.config.kaPlayers,
                CrimsonCombat.config.kaMobs,
                CrimsonCombat.config.kaThroughWalls,
                CrimsonCombat.config.kaPriority
        );

        if (target == null) return;

        float[] rots = TargetUtils.getRotations(target);

        if (CrimsonCombat.config.kaSilent) {
            // Silent — только атакуем, ротацию не меняем визуально
        } else {
            mc.player.setYaw(rots[0]);
            mc.player.setPitch(rots[1]);
        }

        if (mc.player.getAttackCooldownProgress(0.5f) >= 0.9f) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastAttack = System.currentTimeMillis();
        }
    }
}
