package ru.crimson.client.visual;

import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

/**
 * Central NoRender state. Actual cancellation of overlays is done via mixins
 * that call these helpers.
 */
public final class NoRender {

    private NoRender() {}

    public static void init() {
        // State only. Mixins query the helpers below.
    }

    public static boolean isEnabled() {
        return CrimsonClientClient.config.noRender;
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
