package ru.crimson.combat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class CombatConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("crimson-combat.json");

    // KillAura
    public boolean killAura = false;
    public double kaRange = 4.2;
    public int kaCps = 12;
    public boolean kaSilent = true;
    public boolean kaThroughWalls = false;
    public int kaPriority = 0; // 0 - closest, 1 - health, 2 - armor
    public boolean kaPlayers = true;
    public boolean kaMobs = true;
    public float kaFov = 180f;

    // TriggerBot
    public boolean triggerBot = false;
    public double tbRange = 3.5;
    public int tbCps = 14;
    public boolean tbThroughWalls = false;
    public boolean tbPlayers = true;
    public boolean tbMobs = true;

    // AimAssist
    public boolean aimAssist = false;
    public double aaRange = 4.5;
    public float aaSpeed = 0.35f;
    public float aaFov = 90f;
    public boolean aaSilent = false;
    public boolean aaThroughWalls = false;
    public boolean aaPlayers = true;
    public boolean aaMobs = true;

    // ESP
    public boolean esp = true;
    public boolean espPlayers = true;
    public boolean espMobs = false;
    public boolean espThroughWalls = true;

    public void load() {
        try {
            if (Files.exists(FILE)) {
                CombatConfig loaded = GSON.fromJson(Files.readString(FILE), CombatConfig.class);
                if (loaded != null) {
                    this.killAura = loaded.killAura;
                    this.kaRange = loaded.kaRange;
                    this.kaCps = loaded.kaCps;
                    this.kaSilent = loaded.kaSilent;
                    this.kaThroughWalls = loaded.kaThroughWalls;
                    this.kaPriority = loaded.kaPriority;
                    this.kaPlayers = loaded.kaPlayers;
                    this.kaMobs = loaded.kaMobs;
                    this.kaFov = loaded.kaFov;

                    this.triggerBot = loaded.triggerBot;
                    this.tbRange = loaded.tbRange;
                    this.tbCps = loaded.tbCps;
                    this.tbThroughWalls = loaded.tbThroughWalls;
                    this.tbPlayers = loaded.tbPlayers;
                    this.tbMobs = loaded.tbMobs;

                    this.aimAssist = loaded.aimAssist;
                    this.aaRange = loaded.aaRange;
                    this.aaSpeed = loaded.aaSpeed;
                    this.aaFov = loaded.aaFov;
                    this.aaSilent = loaded.aaSilent;
                    this.aaThroughWalls = loaded.aaThroughWalls;
                    this.aaPlayers = loaded.aaPlayers;
                    this.aaMobs = loaded.aaMobs;

                    this.esp = loaded.esp;
                    this.espPlayers = loaded.espPlayers;
                    this.espMobs = loaded.espMobs;
                    this.espThroughWalls = loaded.espThroughWalls;
                }
            }
        } catch (Exception ignored) {}
    }

    public void save() {
        try {
            Files.writeString(FILE, GSON.toJson(this));
        } catch (Exception ignored) {}
    }
}
