package ru.crimson.combat.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.combat.util.TargetUtils;

@Environment(EnvType.CLIENT)
public class AimAssist {

    public void onTick() {
        class_310 mc = class_310.method_1551();
        CrimsonConfig cfg = CrimsonClientClient.config;

        // aaSilent = true means "don't move camera" (silent mode)
        if (cfg == null || !cfg.aimAssist || cfg.aaSilent || mc.field_1724 == null || mc.field_1687 == null) {
            return;
        }

        class_1309 target = TargetUtils.findTarget(
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
        mc.field_1724.method_36456(TargetUtils.smoothAngle(mc.field_1724.method_36454(), rotations[0], speed));
        mc.field_1724.method_36457(TargetUtils.smoothAngle(mc.field_1724.method_36455(), rotations[1], speed * 0.7f));
    }
}
