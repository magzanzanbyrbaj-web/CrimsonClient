package ru.crimson.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class CrimsonConfig {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path FILE =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("crimsonclient.json");

    // RENDER
    public boolean hitParticles = false;
    public boolean criticalEffects = false;
    public boolean hitSound = false;
    public boolean hitmarker = false;
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

    // PLAYER / HUD
    public boolean targetHud = true;
    public boolean armorHud = true;
    public boolean totemCounter = true;
    public boolean inventoryHud = false;
    public boolean potionHud = true;
    public boolean durabilityHud = true;

    // DISPLAY
    public boolean watermark = true;
    public boolean keystrokes = false;
    public boolean moduleList = true;

    // UTILS
    public boolean fullbright = false;
    public boolean reduceParticles = false;
    public boolean fpsBoost = false;
    public boolean optimized = false;
    public boolean glassAnimation = true;

    // VISUAL SETTINGS
    public int chinaHatColor = -1426112427;
    public boolean chinaHatRainbow = false;
    public float chinaHatHeight = 0.5f;
    public float chinaHatRadius = 0.6f;

    public int trailsLength = 40;
    public int trailsColor = -1;
    public boolean trailsFade = true;

    public float jumpCircleSize = 1.0f;
    public float jumpCircleThickness = 1.5f;
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

    public int skyStyle = 0;
    public int skyColor = -1;

    public int hitSoundType = 0;
    public float hitSoundVolume = 1.0f;
    public int criticalStyle = 0;

    // NO RENDER
    public boolean hideFire = false;
    public boolean hideHurtCamera = false;
    public boolean hidePortal = false;
    public boolean hidePumpkin = false;
    public boolean hideVignette = false;
    public boolean hideNausea = false;
    public boolean hideWeather = false;
    public boolean hideParticles = false;

    // CROSSHAIR
    public int crosshairColor = -1;
    public float crosshairSize = 4.0f;
    public float crosshairGap = 2.0f;
    public int crosshairStyle = 0;

    // HUD POSITIONS
    public int watermarkX = 10;
    public int watermarkY = 10;
    public int targetHudX = 10;
    public int targetHudY = 80;
    public int armorHudX = 10;
    public int armorHudY = 160;
    public int totemHudX = 10;
    public int totemHudY = 220;
    public int keystrokesX = 10;
    public int keystrokesY = 280;
    public int moduleListX = 10;
    public int moduleListY = 340;
    public int inventoryHudX = 10;
    public int inventoryHudY = 400;
    public int potionHudX = 10;
    public int potionHudY = 460;
    public int durabilityHudX = 10;
    public int durabilityHudY = 520;

    // KEYBINDS
    // -1 означает, что клавиша не назначена.
    public int bindKillAura = -1;
    public int bindTriggerBot = -1;
    public int bindAimAssist = -1;
    public int bindESP = -1;
    public int bindTargetHUD = -1;
    public int bindArmorHUD = -1;
    public int bindTotemCounter = -1;
    public int bindWatermark = -1;
    public int bindKeystrokes = -1;
    public int bindModuleList = -1;
    public int bindInventoryHUD = -1;
    public int bindPotionHUD = -1;
    public int bindDurabilityHUD = -1;

    // KILL AURA
    public boolean killAura = false;
    public double kaRange = 4.2;
    public int kaCps = 12;
    public boolean kaSilent = true;
    public boolean kaThroughWalls = false;
    public int kaPriority = 0;
    public boolean kaPlayers = true;
    public boolean kaMobs = true;
    public float kaFov = 180f;
    public boolean kaOnlyCriticals = false;
    public float kaSmoothness = 0.28f;

    // TRIGGER BOT
    public boolean triggerBot = false;
    public double tbRange = 3.5;
    public int tbCps = 14;
    public boolean tbThroughWalls = false;
    public boolean tbPlayers = true;
    public boolean tbMobs = true;
    public boolean tbOnlyCriticals = false;

    // AIM ASSIST
    public boolean aimAssist = false;
    public double aaRange = 4.5;
    public float aaSpeed = 0.18f;
    public float aaFov = 90f;
    public boolean aaSilent = false;
    public boolean aaThroughWalls = false;
    public boolean aaPlayers = true;
    public boolean aaMobs = true;

    // ESP
    public boolean esp = false;
    public boolean espPlayers = true;
    public boolean espMobs = false;
    public boolean espThroughWalls = true;

    // CHEST ESP SETTINGS
    public boolean chestEsp = false;
    public boolean chestEspNormal = true;
    public boolean chestEspTrapped = true;
    public boolean chestEspEnder = true;
    public boolean chestEspBarrel = true;
    public boolean chestEspShulker = true;
    public float chestEspLineWidth = 1.5f;
    public int chestEspColor = 0xFFFFB347;
    public boolean chestEspFilled = false;

    // MOVEMENT MODULE SETTINGS
    public boolean freeCamera = false;
    public boolean scaffold = false;
    public boolean spider = false;
    public boolean fly = false;

    // PLAYER MODULE SETTINGS
    public boolean chestStealer = false;

    public static CrimsonConfig load() {
        try {
            if (Files.exists(FILE)) {
                String json = Files.readString(FILE);
                CrimsonConfig loaded =
                        GSON.fromJson(json, CrimsonConfig.class);

                if (loaded != null) {
                    return loaded;
                }
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
