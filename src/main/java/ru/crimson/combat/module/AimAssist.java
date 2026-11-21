package ru.crimson.combat.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import ru.crimson.combat.CrimsonCombat;
import ru.crimson.combat.util.TargetUtils;

public class AimAssist {
    public void onTick(MinecraftClient mc) {
        if (!CrimsonCombat.config.aimAssist || mc.player == null) return;

        LivingEntity target = TargetUtils.findTarget(
                CrimsonCombat.config.aaRange,
                CrimsonCombat.config.aaFov,
                CrimsonCombat.config.aaPlayers,
                CrimsonCombat.config.aaMobs,
                CrimsonCombat.config.aaThroughWalls,
                0
        );

        if (target == null) return;

        float[] targetRots = TargetUtils.getRotations(target);
        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        float yawDiff = wrapDegrees(targetRots[0] - currentYaw);
        float pitchDiff = targetRots[1] - currentPitch;

        float speed = CrimsonCombat.config.aaSpeed;

        if (CrimsonCombat.config.aaSilent) {
            // Можно доработать silent через пакеты, пока оставляем visible
        }

        mc.player.setYaw(currentYaw + yawDiff * speed);
        mc.player.setPitch(currentPitch + pitchDiff * speed);
    }

    private float wrapDegrees(float degrees) {
        degrees %= 360;
        if (degrees >= 180) degrees -= 360;
        if (degrees < -180) degrees += 360;
        return degrees;
    }
}
