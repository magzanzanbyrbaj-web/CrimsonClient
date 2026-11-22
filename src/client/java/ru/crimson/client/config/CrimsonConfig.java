package ru.crimson.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class CrimsonConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("crimsonclient.json");

    // RENDER
    public boolean hitParticles = false, criticalEffects = false, hitSound = false;
    public boolean hitmarker = false, damageNumbers = false, targetEsp = false;
    public boolean customCrosshair = false, customSky = false, chinaHat = false;
    public boolean trails = false, chatAnimations = false, jumpCircle = false;
    public boolean customHitbox = false, blockOverlay = false, noRender = false;
    public boolean inventoryBlur = false, smoothSword = false, itemSwitchAnim = false;
    public boolean totemAnimation = false, reachDisplay = false, pearlTracker = false;

    // PLAYER / HUD
    public boolean targetHud = true, armorHud = true, totemCounter = true;
    public boolean inventoryHud = false, potionHud = true, durabilityHud = true;

    // DISPLAY
    public boolean watermark = true, keystrokes = false, moduleList = true;

    // UTILS
    public boolean fullbright = false, reduceParticles = false, fpsBoost = false;
    public boolean optimized = false, glassAnimation = true;

    // VISUAL SETTINGS
    public int chinaHatColor = -1426112427;
    public boolean chinaHatRainbow = false;
    public float chinaHatHeight = 0.5f, chinaHatRadius = 0.6f;
    public int trailsLength = 40, trailsColor = -1;
    public boolean trailsFade = true;
    public float jumpCircleSize = 1.0f, jumpCircleThickness = 1.5f;
    public int jumpCircleColor = -1;
    public boolean jumpCircleFade = true;
    public float targetEspScale = 1.0f;
    public int targetEspColor = -1;
    public boolean targetEspRing = true;
    public float blockOverlayWidth = 2.0f;
    public boolean blockOverlayGradient = false;
    public int blockOverlayColor = -1;
    public int hitboxColor = -1;
    public float hitboxWidth = 1.5f;
    public int skyStyle = 0, skyColor = -1;
    public int hitSoundType = 0;
    public float hitSoundVolume = 1.0f;
    public int criticalStyle = 0;

    // NO RENDER
    public boolean hideFire = false, hideHurtCamera = false, hidePortal = false;
    public boolean hidePumpkin = false, hideVignette = false, hideNausea = false;
    public boolean hideWeather = false, hideParticles = false;

    // CROSSHAIR
    public int crosshairColor = -1;
    public float crosshairSize = 4.0f, crosshairGap = 2.0f;
    public int crosshairStyle = 0;

    // HUD POSITIONS
    public int watermarkX = 10, watermarkY = 10;
    public int targetHudX = 10, targetHudY = 80;
    public int armorHudX = 10, armorHudY = 160;
    public int totemHudX = 10, totemHudY = 220;
    public int keystrokesX = 10, keystrokesY = 280;
    public int moduleListX = 10, moduleListY = 340;
    public int inventoryHudX = 10, inventoryHudY = 400;
    public int potionHudX = 10, potionHudY = 460;
    public int durabilityHudX = 10, durabilityHudY = 520;

    // KILL AURA
    public boolean killAura = false;
    public double kaRange = 4.2;
    public int kaCps = 12;
    public boolean kaSilent = true, kaThroughWalls = false;
    public int kaPriority = 0;
    public boolean kaPlayers = true, kaMobs = true;
    public float kaFov = 180f;
    public boolean kaOnlyCriticals = false;
    public float kaSmoothness = 0.28f;

    // TRIGGER BOT
    public boolean triggerBot = false;
    public double tbRange = 3.5;
    public int tbCps = 14;
    public boolean tbThroughWalls = false;
    public boolean tbPlayers = true, tbMobs = true;
    public boolean tbOnlyCriticals = false;

    // AIM ASSIST
    public boolean aimAssist = false;
    public double aaRange = 4.5;
    public float aaSpeed = 0.18f;
    public float aaFov = 90f;
    public boolean aaSilent = false, aaThroughWalls = false;
    public boolean aaPlayers = true, aaMobs = true;

    // ESP
    public boolean esp = false;
    public boolean espPlayers = true, espMobs = false;
    public boolean espThroughWalls = true;

    public static CrimsonConfig load() {
        try {
            if (Files.exists(FILE)) {
                CrimsonConfig loaded = GSON.fromJson(Files.readString(FILE), CrimsonConfig.class);
                if (loaded != null) return loaded;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new CrimsonConfig();
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
