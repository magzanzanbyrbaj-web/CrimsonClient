package ru.crimson.combat.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.combat.util.TargetUtils;

public class ESP {

    public void onTick() {
        // Логика рендера боксов обычно делается через WorldRenderEvents.
        // Пока оставляем заглушку + фильтрацию, чтобы модуль просто работал.
        Minecraft mc = Minecraft.getInstance();
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.esp || mc.player == null || mc.level == null) return;

        // Здесь можно потом добавить реальный рендер.
        // Сейчас модуль просто активен и готов к расширению.
    }

    public static boolean shouldRender(Entity entity) {
        CrimsonConfig cfg = CrimsonClientClient.config;
        if (cfg == null || !cfg.esp) return false;

        if (!(entity instanceof LivingEntity living) || !living.isAlive()) return false;
        if (living == Minecraft.getInstance().player) return false;

        boolean isPlayer = living instanceof Player;
        if (isPlayer && !cfg.espPlayers) return false;
        if (!isPlayer && !cfg.espMobs) return false;

        if (!cfg.espThroughWalls && !TargetUtils.canSee(living)) return false;

        return true;
    }
}
