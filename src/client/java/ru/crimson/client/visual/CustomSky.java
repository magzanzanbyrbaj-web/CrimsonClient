package ru.crimson.client.visual;

import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

public final class CustomSky {
    private CustomSky() {}

    public static void init() {
        // Цвет неба читается через getSkyColor().
    }

    public static boolean isEnabled() {
        return CrimsonClientClient.config.customSky;
    }

    public static int getSkyColor() {
        CrimsonConfig config = CrimsonClientClient.config;

        if (!config.customSky) {
            return 0xFF87CEEB;
        }

        if (config.skyStyle == 1) {
            long time = System.currentTimeMillis() % 3000L;

            float pulse = (float) (
                    0.6 + 0.4 * Math.sin(
                            time / 3000.0 * Math.PI * 2
                    )
            );

            int base = config.skyColor;

            int r = (int) (((base >> 16) & 255) * pulse);
            int g = (int) (((base >> 8) & 255) * pulse);
            int b = (int) ((base & 255) * pulse);

            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }

        return config.skyColor;
    }

    public static float[] getSkyRgb() {
        int color = getSkyColor();

        return new float[]{
                ((color >> 16) & 255) / 255f,
                ((color >> 8) & 255) / 255f,
                (color & 255) / 255f
        };
    }
}
