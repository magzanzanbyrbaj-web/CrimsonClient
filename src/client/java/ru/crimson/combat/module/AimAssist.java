package ru.crimson.combat.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.combat.util.TargetUtils;

@Environment(EnvType.CLIENT)
public class AimAssist {

    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        CrimsonConfig cfg = CrimsonClientClient.config;

        // aaSilent = true means "don't move camera" (silent mode)
        if (cfg == null || !cfg.aimAssist || cfg.aaSilent || mc.player == null || mc.level == null) {
            return;
        }

        LivingEntity target = TargetUtils.findTarget(
                cfg.aaRange,
                cfg.aaFov,
                cfg.aaPlayers,
                cfg.aaMobs,
                cfg.aaThroughWalls,
                0 // closest
        );

        if (target == null) {
            return;
        }

        float[] rotations = TargetUtils.getRotations(target);
        float speed = Math.max(0.01f, Math.min(0.55f, cfg.aaSpeed));

        // Horizontal more aggressive, vertical a bit softer
        mc.player.setYRot(TargetUtils.smoothAngle(mc.player.getYRot(), rotations[0], speed));
        mc.player.setXRot(TargetUtils.smoothAngle(mc.player.getXRot(), rotations[1], speed * 0.7f));
    }
}
