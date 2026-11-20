
package ru.crimson.client.visual;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

public final class NoRender {

    private NoRender() {}

    public static void init() {
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player == null || mc.level == null) return;

            CrimsonConfig c = CrimsonClientClient.config;
            if (!c.noRender) return;

            // HUD overlays are handled by their respective render hooks.
            // This class provides the central toggle state.
        });
    }

    public static boolean hideFire() {
        CrimsonConfig c = CrimsonClientClient.config;
        return c.noRender && c.hideFire;
    }

    public static boolean hideHurtCamera() {
        CrimsonConfig c = CrimsonClientClient.config;
        return c.noRender && c.hideHurtCamera;
    }

    public static boolean hidePortal() {
        CrimsonConfig c = CrimsonClientClient.config;
        return c.noRender && c.hidePortal;
    }

    public static boolean hidePumpkin() {
        CrimsonConfig c = CrimsonClientClient.config;
        return c.noRender && c.hidePumpkin;
    }

    public static boolean hideVignette() {
        CrimsonConfig c = CrimsonClientClient.config;
        return c.noRender && c.hideVignette;
    }

    public static boolean hideNausea() {
        CrimsonConfig c = CrimsonClientClient.config;
        return c.noRender && c.hideNausea;
    }

    public static boolean hideWeather() {
        CrimsonConfig c = CrimsonClientClient.config;
        return c.noRender && c.hideWeather;
    }

    public static boolean hideParticles() {
        CrimsonConfig c = CrimsonClientClient.config;
        return c.noRender && c.hideParticles;
    }
}
