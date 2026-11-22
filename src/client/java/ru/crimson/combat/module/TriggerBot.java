package ru.crimson.combat.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_239;
import net.minecraft.class_310;
import net.minecraft.class_3966;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.client.hud.HudRenderer;
import ru.crimson.combat.util.TargetUtils;

@Environment(EnvType.CLIENT)
public class TriggerBot {
    private long lastAttack = 0L;

    public void onTick() {
        class_310 mc = class_310.method_1551();
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.triggerBot || mc.field_1724 == null || mc.field_1687 == null || mc.field_1761 == null) {
            return;
        }

        // Only when looking at an entity
        if (mc.field_1765 == null || mc.field_1765.method_17783() != class_239.class_240.field_1331) {
            return;
        }

        class_1297 entity = ((class_3966) mc.field_1765).method_17782();
        if (!(entity instanceof class_1309 target) || !target.method_5805()) {
            return;
        }

        boolean isPlayer = target instanceof class_1657;
        if (isPlayer && !cfg.tbPlayers) return;
        if (!isPlayer && !cfg.tbMobs) return;

        if ((double) mc.field_1724.method_5739(target) > cfg.tbRange) {
            return;
        }

        if (!cfg.tbThroughWalls && !TargetUtils.canSee(target)) {
            return;
        }

        if (cfg.tbOnlyCriticals && !TargetUtils.isCriticalHitReady()) {
            return;
        }

        long delay = (long) (1000.0 / Math.max(1.0, Math.min(20.0, cfg.tbCps)));
        long now = System.currentTimeMillis();
        if (now - lastAttack < delay) {
            return;
        }

        if (mc.field_1724.method_7261(0.5f) < 0.9f) {
            return;
        }

        mc.field_1761.method_2918((class_1657) mc.field_1724, target);
        mc.field_1724.method_6104(class_1268.field_5808);

        try {
            HudRenderer.setTarget(target);
        } catch (Throwable ignored) {}

        lastAttack = now;
    }
}
