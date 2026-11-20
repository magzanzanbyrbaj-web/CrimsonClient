package ru.crimson.client.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.crimson.client.CrimsonClientClient;

@Mixin(Gui.class)
public class InGameHudMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void crimson$hideVanillaCrosshair(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (CrimsonClientClient.config != null && CrimsonClientClient.config.customCrosshair) {
            ci.cancel();
        }
    }
}
