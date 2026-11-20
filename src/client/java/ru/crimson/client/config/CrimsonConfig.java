
package ru.crimson.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import ru.crimson.client.CrimsonClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CrimsonConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("crimsonclient.json");

    public boolean watermark = true;
    public boolean targetHud = true;
    public boolean armorHud = true;
    public boolean totemCounter = true;
    public boolean keystrokes = true;
    public boolean moduleList = true;
    public boolean inventoryHud = true;
    public boolean potionHud = true;
    public boolean durabilityHud = true;

    public boolean hitParticles = true;
    public boolean damageNumbers = true;
    public boolean hitmarker = true;
    public boolean criticalEffects = true;
    public boolean smoothSword = false;
    public boolean itemSwitchAnim = true;
    public boolean totemAnimation = true;
    public boolean customCrosshair = false;
    public boolean customSky = false;
    public boolean inventoryBlur = true;

    public boolean fullbright = false;
    public boolean reduceParticles = true;
    public boolean optimized = true;
    public boolean glassAnimation = true;

    public float menuScale = 1.0f;
    public int particleLimit = 50;

    public String watermarkText = "Crimson";

    // HUD positions. -1 means automatic position.
    public int watermarkX = -1;
    public int watermarkY = 7;

    public int targetHudX = -1;
    public int targetHudY = -1;

    public int armorHudX = -1;
    public int armorHudY = -1;

    public int totemHudX = -1;
    public int totemHudY = -1;

    public int keystrokesX = 5;
    public int keystrokesY = 43;

    public int moduleListX = 5;
    public int moduleListY = 100;

    public int inventoryHudX = -1;
    public int inventoryHudY = -1;

    public int potionHudX = -1;
    public int potionHudY = 42;

    public int durabilityHudX = -1;
    public int durabilityHudY = -1;

    public static CrimsonConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                CrimsonConfig cfg = GSON.fromJson(
                        Files.readString(CONFIG_PATH),
                        CrimsonConfig.class
                );
                if (cfg != null) return cfg;
            } catch (Exception e) {
                CrimsonClient.LOGGER.error("Failed to load config", e);
            }
        }

        CrimsonConfig cfg = new CrimsonConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            CrimsonClient.LOGGER.error("Failed to save config", e);
        }
    }
}
