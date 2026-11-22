package ru.crimson.combat.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.combat.util.TargetUtils;

public class AimAssist {
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        CrimsonConfig cfg = CrimsonClientClient.config;
        if (cfg == null || !cfg.aimAssist || cfg.aaSilent || mc.player == null || mc.level == null) return;

        LivingEntity target = TargetUtils.findTarget(cfg.aaRange, cfg.aaFov,
                cfg.aaPlayers, cfg.aaMobs, cfg.aaThroughWalls, 0);
        if (target == null) return;

        float[] rotations = TargetUtils.getRotations(target);
        float speed = Math.max(0.01f, Math.min(0.45f, cfg.aaSpeed));
        mc.player.setYRot(TargetUtils.smoothAngle(mc.player.getYRot(), rotations[0], speed));
        mc.player.setXRot(TargetUtils.smoothAngle(mc.player.getXRot(), rotations[1], speed * 0.75f));
    }
}
