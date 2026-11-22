package ru.crimson.combat.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.client.hud.HudRenderer;
import ru.crimson.combat.util.TargetUtils;

@Environment(EnvType.CLIENT)
public class KillAura {
    private long lastAttack = 0L;

    public void onTick() {
        class_310 mc = class_310.method_1551();
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.killAura || mc.field_1724 == null || mc.field_1687 == null || mc.field_1761 == null) {
            return;
        }

        class_1309 target = TargetUtils.findTarget(
                cfg.kaRange,
                cfg.kaFov,
                cfg.kaPlayers,
                cfg.kaMobs,
                cfg.kaThroughWalls,
                cfg.kaPriority
        );

        if (target == null) {
            return;
        }

        // Rotation (non-silent)
        if (!cfg.kaSilent) {
            float[] rotations = TargetUtils.getRotations(target);
            float smooth = Math.max(0.02f, Math.min(1.0f, cfg.kaSmoothness));
            mc.field_1724.method_36456(TargetUtils.smoothAngle(mc.field_1724.method_36454(), rotations[0], smooth));
            mc.field_1724.method_36457(TargetUtils.smoothAngle(mc.field_1724.method_36455(), rotations[1], smooth));
        }

        if (cfg.kaOnlyCriticals && !TargetUtils.isCriticalHitReady()) {
            return;
        }

        // CPS limit
        long delay = (long) (1000.0 / Math.max(1.0, Math.min(20.0, cfg.kaCps)));
        long now = System.currentTimeMillis();
        if (now - lastAttack < delay) {
            return;
        }

        // Attack cooldown check (1.21 style)
        if (mc.field_1724.method_7261(0.5f) < 0.9f) {
            return;
        }

        // Attack
        mc.field_1761.method_2918((class_1657) mc.field_1724, (class_1297) target);
        mc.field_1724.method_6104(class_1268.field_5808);

        // HUD target
        try {
            HudRenderer.setTarget(target);
        } catch (Throwable ignored) {}

        lastAttack = now;
    }
}
