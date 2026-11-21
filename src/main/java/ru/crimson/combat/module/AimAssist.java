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

        if (cfg == null || !cfg.aimAssist || mc.player == null || mc.level == null) return;

        LivingEntity target = TargetUtils.findTarget(
                cfg.aaRange,
                cfg.aaFov,
                cfg.aaPlayers,
                cfg.aaMobs,
                cfg.aaThroughWalls,
                0
        );

        if (target == null) return;

        float[] targetRots = TargetUtils.getRotations(target);

        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();

        float yawDiff = wrapDegrees(targetRots[0] - currentYaw);
        float pitchDiff = targetRots[1] - currentPitch;

        float speed = cfg.aaSpeed;

        if (!cfg.aaSilent) {
            mc.player.setYRot(currentYaw + yawDiff * speed);
            mc.player.setXRot(currentPitch + pitchDiff * speed);
        }
    }

    private float wrapDegrees(float degrees) {
        degrees %= 360f;
        if (degrees >= 180f) degrees -= 360f;
        if (degrees < -180f) degrees += 360f;
        return degrees;
    }
}
