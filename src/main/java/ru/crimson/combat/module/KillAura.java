package ru.crimson.combat.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.combat.util.TargetUtils;

public class KillAura {

    private long lastAttack = 0;

    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.killAura || mc.player == null || mc.level == null) return;

        long delay = 1000L / Math.max(1, cfg.kaCps);
        if (System.currentTimeMillis() - lastAttack < delay) return;

        LivingEntity target = TargetUtils.findTarget(
                cfg.kaRange,
                cfg.kaFov,
                cfg.kaPlayers,
                cfg.kaMobs,
                cfg.kaThroughWalls,
                cfg.kaPriority
        );

        if (target == null) return;

        float[] rots = TargetUtils.getRotations(target);

        if (!cfg.kaSilent) {
            mc.player.setYRot(rots[0]);
            mc.player.setXRot(rots[1]);
        }

        if (mc.player.getAttackStrengthScale(0.5f) >= 0.9f) {
            mc.gameMode.attack(mc.player, target);
            mc.player.swing(InteractionHand.MAIN_HAND);
            lastAttack = System.currentTimeMillis();
        }
    }
}
