package ru.crimson.combat.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import ru.crimson.combat.CrimsonCombat;

public class ESP {
    public void onTick(MinecraftClient mc) {
        // Базовая заготовка. Для нормального ESP лучше добавить WorldRenderEvents
        // и рисовать боксы через BufferBuilder / RenderSystem.
        // Пока просто помечаем, что модуль работает.
        if (!CrimsonCombat.config.esp) return;
    }
}
