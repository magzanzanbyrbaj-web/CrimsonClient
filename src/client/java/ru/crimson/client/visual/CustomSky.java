package ru.crimson.client.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

/**
 * Custom sky tint / fog color helper.
 * Actual sky rendering is hooked via GameRendererMixin / Dimension effects if needed.
 * This class provides the current color and a lightweight fog tint.
 */
public final class CustomSky {

    private CustomSky() {}

    public static void init() {
        // Fog / clear color can be influenced via mixins.
        // For now we expose the color and a simple tick-based pulse.
    }

    public static boolean isEnabled() {
        return CrimsonClientClient.config.customSky;
    }

    public static int getSkyColor() {
        CrimsonConfig c = CrimsonClientClient.config;
        if (!c.customSky) return 0xFF87CEEB; // default sky-ish

        if (c.skyStyle == 1) {
            // Animated crimson pulse
            long t = System.currentTimeMillis() % 3000;
            float pulse = (float) (0.6 + 0.4 * Math.sin(t / 3000.0 * Math.PI * 2));
            int base = c.skyColor;
            int r = (int) (((base >> 16) & 0xFF) * pulse);
            int g = (int) (((base >> 8) & 0xFF) * pulse);
            int b = (int) ((base & 0xFF) * pulse);
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }

        return c.skyColor;
    }

    public static float[] getSkyRgb() {
        int color = getSkyColor();
        return new float[]{
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f,
                (color & 0xFF) / 255f
        };
    }
}
