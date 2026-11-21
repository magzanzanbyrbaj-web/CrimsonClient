
package ru.crimson.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CrimsonConfig {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path FILE =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("crimsonclient")
                    .resolve("config.json");

    // RENDER
    public boolean hitParticles = true;
    public boolean criticalEffects = false;
    public boolean hitSound = false;
    public boolean hitmarker = true;
    public boolean damageNumbers = false;
    public boolean targetEsp = false;
    public boolean customCrosshair = false;
    public boolean customSky = false;
    public boolean chinaHat = false;
    public boolean trails = false;
    public boolean chatAnimations = false;
    public boolean jumpCircle = false;
    public boolean customHitbox = false;
    public boolean blockOverlay = false;
    public boolean noRender = false;
    public boolean inventoryBlur = false;
    public boolean smoothSword = false;
    public boolean itemSwitchAnim = false;
    public boolean totemAnimation = false;
    public boolean reachDisplay = false;
    public boolean pearlTracker = false;

    // PLAYER HUD
    public boolean targetHud = true;
    public boolean armorHud = true;
    public boolean totemCounter = true;
    public boolean inventoryHud = false;
    public boolean potionHud = true;
    public boolean durabilityHud = true;

    // DISPLAY HUD
    public boolean watermark = true;
    public boolean keystrokes = false;
    public boolean moduleList = true;

    // PERFORMANCE
    public boolean fullbright = false;
    public boolean reduceParticles = false;
    public boolean fpsBoost = false;
    public boolean optimized = false;
    public boolean glassAnimation = true;

    // CHINA HAT
    public int chinaHatColor = 0xAAFF4055;
    public boolean chinaHatRainbow = false;
    public float chinaHatHeight = 0.35f;
    public float chinaHatRadius = 0.65f;

    // TRAILS
    public int trailsLength = 40;
    public int trailsColor = 0xCCFF4055;
    public boolean trailsFade = true;

    // JUMP CIRCLE
    public float jumpCircleSize = 0.8f;
    public float jumpCircleThickness = 2.5f;
    public int jumpCircleColor = 0xFFFF4055;
    public boolean jumpCircleFade = true;

    // TARGET ESP
    public float targetEspScale = 1.0f;
    public int targetEspColor = 0xFFFF4055;
    public boolean targetEspRing = true;

    // BLOCK OVERLAY
    public float blockOverlayWidth = 2.0f;
    public boolean blockOverlayGradient = true;
    public int blockOverlayColor = 0xFFFF4055;

    // HITBOX
    public int hitboxColor = 0xAAFF4055;
    public float hitboxWidth = 2.0f;

    // SKY
    public int skyStyle = 0;
    public int skyColor = 0xFF17101F;

    // HIT SOUND
    public int hitSoundType = 0;
    public float hitSoundVolume = 0.8f;

    // CRITICAL EFFECTS
    public int criticalStyle = 0;

    // HUD POSITIONS
    public int watermarkX = -1;
    public int watermarkY = -1;

    public int targetHudX = -1;
    public int targetHudY = -1;

    public int armorHudX = -1;
    public int armorHudY = -1;

    public int totemHudX = -1;
    public int totemHudY = -1;

    public int keystrokesX = 10;
    public int keystrokesY = 60;

    public int moduleListX = 10;
    public int moduleListY = 40;

    public int inventoryHudX = -1;
    public int inventoryHudY = -1;

    public int potionHudX = -1;
    public int potionHudY = 40;

    public int durabilityHudX = -1;
    public int durabilityHudY = -1;

    // CROSSHAIR
    public int crosshairColor = 0xFFFF4055;
    public float crosshairSize = 5.0f;
    public float crosshairGap = 3.0f;
    public int crosshairStyle = 0;

    // NO RENDER
    public boolean hideFire = false;

    public static CrimsonConfig load() {
        try {
            Files.createDirectories(FILE.getParent());

            if (!Files.exists(FILE)) {
                CrimsonConfig config = new CrimsonConfig();
                config.save();
                return config;
            }

            CrimsonConfig config = GSON.fromJson(
                    Files.readString(FILE),
                    CrimsonConfig.class
            );

            if (config == null) {
                config = new CrimsonConfig();
            }

            return config;

        } catch (Exception exception) {
            exception.printStackTrace();
            return new CrimsonConfig();
        }
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static Path getFile() {
        return FILE;
    }
}
