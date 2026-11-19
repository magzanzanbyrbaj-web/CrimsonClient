package ru.crimson.client.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	// Placeholder for future visual / optimization hooks.
	// Fullbright is applied via options.gamma in client tick.
}
