
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
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("crimsonclient.json");

    // HUD
    public boolean watermark = true;
    public boolean targetHud = true;
    public boolean armorHud = true;
    public boolean totemCounter = true;
    public boolean keystrokes = true;
    public boolean moduleList = true;
    public boolean inventoryHud = true;
    public boolean potionHud = true;
    public boolean durabilityHud = true;

    // Combat visuals
    public boolean hitParticles = true;
    public boolean damageNumbers = true;
    public boolean hitmarker = true;
    public boolean criticalEffects = true;
    public boolean targetEsp = false;
    public boolean hitSound = false;

    // Visuals
    public boolean smoothSword = false;
    public boolean itemSwitchAnim = true;
    public boolean totemAnimation = true;
    public boolean customCrosshair = false;
    public boolean customSky = false;
    public boolean inventoryBlur = true;
    public boolean jumpCircle = false;
    public boolean customHitbox = false;
    public boolean blockOverlay = true;

    // No Render
    public boolean noRender = false;
    public boolean hideFire = true;
    public boolean hideHurtCamera = false;
    public boolean hidePortal = false;
    public boolean hidePumpkin = false;
    public boolean hideVignette = false;
    public boolean hideNausea = false;
    public boolean hideWeather = false;
    public boolean hideParticles = false;

    // Target ESP
    public boolean targetEspParticles = true;
    public boolean targetEspRing = true;
    public int targetEspStyle = 0;
    public int targetEspColor = 0xFFFF4055;

    // Hit Sound
    public int hitSoundType = 0;
    public float hitSoundVolume = 0.8f;

    // Critical Effects: 0 Staircase, 1 Ghosts, 2 Spiral, 3 Burst
    public int criticalStyle = 0;

    // Crosshair
    public int crosshairStyle = 0;
    public int crosshairColor = 0xFFFF4055;
    public float crosshairSize = 5.0f;
    public float crosshairGap = 3.0f;

    // Jump Circle
    public int jumpCircleColor = 0xFFFF4055;
    public float jumpCircleSize = 1.0f;
    public float jumpCircleThickness = 4.5f;
    public boolean jumpCircleFade = true;

    // Hitbox
    public int hitboxColor = 0xFFFF4055;
    public float hitboxWidth = 1.5f;

    // Sky
    public int skyColor = 0xFF48202F;
    public int skyStyle = 0;

    // Block Overlay
    public int blockOverlayStyle = 0;
    public int blockOverlayColor = 0xFFFF4055;
    public boolean blockOverlayGradient = true;
    public float blockOverlayWidth = 2.0f;

    // General
    public boolean fullbright = false;
    public boolean reduceParticles = true;
    public boolean optimized = true;
    public boolean glassAnimation = true;

    public float menuScale = 1.0f;
    public int particleLimit = 50;
    public String watermarkText = "Crimson";

    // HUD positions
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
                CrimsonConfig config = GSON.fromJson(
                        Files.readString(CONFIG_PATH),
                        CrimsonConfig.class
                );
                if (config != null) return config;
            } catch (Exception e) {
                CrimsonClient.LOGGER.error("Failed to load Crimson config", e);
            }
        }

        CrimsonConfig config = new CrimsonConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            CrimsonClient.LOGGER.error("Failed to save Crimson config", e);
        }
    }
}
