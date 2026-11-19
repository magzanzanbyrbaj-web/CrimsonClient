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
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("crimsonclient.json");

	// === HUD ===
	public boolean watermark = true;
	public boolean targetHud = true;
	public boolean armorHud = true;
	public boolean totemCounter = true;
	public boolean keystrokes = true;
	public boolean moduleList = true;
	public boolean inventoryHud = false;
	public boolean potionHud = true;
	public boolean durabilityHud = true;

	// === Visuals / Effects ===
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

	// === Utilities ===
	public boolean fullbright = false;
	public boolean reduceParticles = true;
	public boolean optimized = true;
	public boolean glassAnimation = true;

	// Settings
	public float menuScale = 1.0f;
	public int particleLimit = 50; // percent of default
	public String watermarkText = "Crimson";
	public int watermarkX = 4;
	public int watermarkY = 4;

	public static CrimsonConfig load() {
		if (Files.exists(CONFIG_PATH)) {
			try {
				String json = Files.readString(CONFIG_PATH);
				CrimsonConfig cfg = GSON.fromJson(json, CrimsonConfig.class);
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
