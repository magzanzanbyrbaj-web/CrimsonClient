package ru.crimson.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.combat.module.AimAssist;
import ru.crimson.combat.module.ChestESP;
import ru.crimson.combat.module.KillAura;
import ru.crimson.combat.module.TriggerBot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CrimsonClientClient implements ClientModInitializer {

    public static CrimsonConfig config;

    private static final Map<Integer, Boolean> previousKeyStates = new HashMap<>();
    private static KeyMapping openMenuKey;

    private KillAura killAuraModule;
    private TriggerBot triggerBotModule;
    private AimAssist aimAssistModule;
    private ChestESP chestEspModule;

    @Override
    public void onInitializeClient() {
        config = CrimsonConfig.load();

        registerKeys();
        registerBindCommand();

        initializeVisuals();
        initializeCombatModules();

        registerTickHandler();
    }

    private void registerKeys() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.crimsonclient.open_menu",
                        GLFW.GLFW_KEY_RIGHT_SHIFT,
                        KeyMapping.Category.MISC
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.consumeClick()) {
                client.setScreen(new ru.crimson.client.gui.CrimsonMenuScreen());
            }
        });
    }

    private void registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                previousKeyStates.clear();
                return;
            }

            handleCustomBinds(client);

            if (config != null) {
                tickCombatModules();
            }
        });
    }

    private void tickCombatModules() {
        runSafe(killAuraModule, () -> killAuraModule.onTick());
        runSafe(triggerBotModule, () -> triggerBotModule.onTick());
        runSafe(aimAssistModule, () -> aimAssistModule.onTick());
    }

    private void runSafe(Object module, Runnable action) {
        if (module == null) {
            return;
        }
        try {
            action.run();
        } catch (Throwable ignored) {
            // Изолируем ошибки отдельных модулей, чтобы не ронять клиентский тик
        }
    }

    private void handleCustomBinds(Minecraft client) {
        if (config == null || config.moduleBinds == null) {
            return;
        }

        if (client.screen != null) {
            previousKeyStates.clear();
            return;
        }

        long window = client.getWindow().handle();
        Set<Integer> keys = new HashSet<>(config.moduleBinds.values());

        for (int key : keys) {
            if (key <= 0 || key > GLFW.GLFW_KEY_LAST) {
                continue;
            }

            boolean down = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
            boolean wasDown = previousKeyStates.getOrDefault(key, false);

            if (down && !wasDown) {
                for (Map.Entry<String, Integer> entry : config.moduleBinds.entrySet()) {
                    Integer boundKey = entry.getValue();
                    if (boundKey != null && boundKey == key) {
                        toggleModule(entry.getKey());
                    }
                }
            }

            previousKeyStates.put(key, down);
        }
    }

    private void toggleModule(String name) {
        switch (name) {
            case "KillAura" -> config.killAura = !config.killAura;
            case "AimAssist" -> config.aimAssist = !config.aimAssist;
            case "TriggerBot" -> config.triggerBot = !config.triggerBot;
            case "ESP" -> config.esp = !config.esp;
            case "ChestESP" -> config.chestEsp = !config.chestEsp;
            case "HitParticles" -> config.hitParticles = !config.hitParticles;
            case "CriticalEffects" -> config.criticalEffects = !config.criticalEffects;
            case "HitSound" -> config.hitSound = !config.hitSound;
            case "Hitmarker" -> config.hitmarker = !config.hitmarker;
            case "DamageNumbers" -> config.damageNumbers = !config.damageNumbers;
            case "TargetESP" -> config.targetEsp = !config.targetEsp;

            case "FreeCamera" -> config.freeCamera = !config.freeCamera;
            case "Scaffold" -> config.scaffold = !config.scaffold;
            case "Spider" -> config.spider = !config.spider;
            case "Fly" -> config.fly = !config.fly;
            case "Trails" -> config.trails = !config.trails;
            case "JumpCircle" -> config.jumpCircle = !config.jumpCircle;
            case "SmoothSword" -> config.smoothSword = !config.smoothSword;
            case "ItemSwitch" -> config.itemSwitchAnim = !config.itemSwitchAnim;

            case "CustomCrosshair" -> config.customCrosshair = !config.customCrosshair;
            case "CustomSky" -> config.customSky = !config.customSky;
            case "ChinaHat" -> config.chinaHat = !config.chinaHat;
            case "BlockOverlay" -> config.blockOverlay = !config.blockOverlay;
            case "NoRender" -> config.noRender = !config.noRender;
            case "InventoryBlur" -> config.inventoryBlur = !config.inventoryBlur;
            case "TotemAnimation" -> config.totemAnimation = !config.totemAnimation;
            case "Fullbright" -> config.fullbright = !config.fullbright;
            case "ReduceParticles" -> config.reduceParticles = !config.reduceParticles;

            case "ChestStealer" -> config.chestStealer = !config.chestStealer;
            case "TargetHUD" -> config.targetHud = !config.targetHud;
            case "ArmorHUD" -> config.armorHud = !config.armorHud;
            case "TotemCounter" -> config.totemCounter = !config.totemCounter;
            case "InventoryHUD" -> config.inventoryHud = !config.inventoryHud;
            case "PotionHUD" -> config.potionHud = !config.potionHud;
            case "DurabilityHUD" -> config.durabilityHud = !config.durabilityHud;
            case "Keystrokes" -> config.keystrokes = !config.keystrokes;

            case "Watermark" -> config.watermark = !config.watermark;
            case "ModuleList" -> config.moduleList = !config.moduleList;
            case "FPSBoost" -> config.fpsBoost = !config.fpsBoost;
            case "Optimization" -> config.optimized = !config.optimized;
            case "MenuAnimation" -> config.glassAnimation = !config.glassAnimation;

            default -> {
                return;
            }
        }

        config.save();

        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.displayClientMessage(
                    Component.literal(
                            "§c[Crimson] §f" + name + ": " +
                                    (isEnabled(name) ? "§aON" : "§cOFF")
                    ),
                    true
            );
        }
    }

    private boolean isEnabled(String name) {
        return switch (name) {
            case "KillAura" -> config.killAura;
            case "AimAssist" -> config.aimAssist;
            case "TriggerBot" -> config.triggerBot;
            case "ESP" -> config.esp;
            case "ChestESP" -> config.chestEsp;
            case "HitParticles" -> config.hitParticles;
            case "CriticalEffects" -> config.criticalEffects;
            case "HitSound" -> config.hitSound;
            case "Hitmarker" -> config.hitmarker;
            case "DamageNumbers" -> config.damageNumbers;
            case "TargetESP" -> config.targetEsp;

            case "FreeCamera" -> config.freeCamera;
            case "Scaffold" -> config.scaffold;
            case "Spider" -> config.spider;
            case "Fly" -> config.fly;
            case "Trails" -> config.trails;
            case "JumpCircle" -> config.jumpCircle;
            case "SmoothSword" -> config.smoothSword;
            case "ItemSwitch" -> config.itemSwitchAnim;

            case "CustomCrosshair" -> config.customCrosshair;
            case "CustomSky" -> config.customSky;
            case "ChinaHat" -> config.chinaHat;
            case "BlockOverlay" -> config.blockOverlay;
            case "NoRender" -> config.noRender;
            case "InventoryBlur" -> config.inventoryBlur;
            case "TotemAnimation" -> config.totemAnimation;
            case "Fullbright" -> config.fullbright;
            case "ReduceParticles" -> config.reduceParticles;

            case "ChestStealer" -> config.chestStealer;
            case "TargetHUD" -> config.targetHud;
            case "ArmorHUD" -> config.armorHud;
            case "TotemCounter" -> config.totemCounter;
            case "InventoryHUD" -> config.inventoryHud;
            case "PotionHUD" -> config.potionHud;
            case "DurabilityHUD" -> config.durabilityHud;
            case "Keystrokes" -> config.keystrokes;

            case "Watermark" -> config.watermark;
            case "ModuleList" -> config.moduleList;
            case "FPSBoost" -> config.fpsBoost;
            case "Optimization" -> config.optimized;
            case "MenuAnimation" -> config.glassAnimation;

            default -> false;
        };
    }

    private void registerBindCommand() {
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            String trimmed = message.trim();

            if (!trimmed.equalsIgnoreCase(".bind") &&
                    !trimmed.toLowerCase(Locale.ROOT).startsWith(".bind ")) {
                return true;
            }

            String[] args = trimmed.split("\\s+");

            if (args.length == 2 && args[1].equalsIgnoreCase("list")) {
                if (config.moduleBinds.isEmpty()) {
                    sendMessage("Нет назначенных биндов.");
                } else {
                    config.moduleBinds.forEach((module, key) ->
                            sendMessage(module + " -> " + keyName(key))
                    );
                }
                return false;
            }

            if (args.length != 3) {
                sendMessage("Использование: .bind <module> <key|none>");
                return false;
            }

            String module = findModule(args[1]);
            if (module == null) {
                sendMessage("Неизвестная функция: " + args[1]);
                return false;
            }

            if (args[2].equalsIgnoreCase("none")) {
                config.setBind(module, -1);
                sendMessage("Бинд удалён: " + module);
                return false;
            }

            int key = parseKey(args[2]);
            if (key < 0) {
                sendMessage("Неизвестная клавиша: " + args[2]);
                return false;
            }

            config.setBind(module, key);
            sendMessage("Назначено: " + module + " -> " + keyName(key));
            return false;
        });
    }

    private String findModule(String input) {
        String wanted = normalize(input);

        String[] modules = {
                "KillAura", "AimAssist", "TriggerBot", "ESP",
                "ChestESP", "HitParticles", "CriticalEffects",
                "HitSound", "Hitmarker", "DamageNumbers",
                "TargetESP", "FreeCamera", "Scaffold", "Spider",
                "Fly", "Trails", "JumpCircle", "SmoothSword",
                "ItemSwitch", "CustomCrosshair", "CustomSky",
                "ChinaHat", "BlockOverlay", "NoRender",
                "InventoryBlur", "TotemAnimation", "Fullbright",
                "ReduceParticles", "ChestStealer", "TargetHUD",
                "ArmorHUD", "TotemCounter", "InventoryHUD",
                "PotionHUD", "DurabilityHUD", "Keystrokes",
                "Watermark", "ModuleList", "FPSBoost",
                "Optimization", "MenuAnimation"
        };

        for (String module : modules) {
            if (normalize(module).equals(wanted)) {
                return module;
            }
        }

        return null;
    }

    private String normalize(String value) {
        return value.replace("_", "")
                .replace("-", "")
                .toLowerCase(Locale.ROOT);
    }

    private int parseKey(String value) {
        String key = value.toUpperCase(Locale.ROOT);

        if (key.length() == 1) {
            char c = key.charAt(0);
            if (c >= 'A' && c <= 'Z') {
                return GLFW.GLFW_KEY_A + c - 'A';
            }
            if (c >= '0' && c <= '9') {
                return GLFW.GLFW_KEY_0 + c - '0';
            }
        }

        if (key.matches("F([1-9]|1[0-2])")) {
            return GLFW.GLFW_KEY_F1 + Integer.parseInt(key.substring(1)) - 1;
        }

        return switch (key) {
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "SHIFT", "LSHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "CTRL", "LCTRL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "RCTRL" -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "ALT", "LALT" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "RALT" -> GLFW.GLFW_KEY_RIGHT_ALT;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "ENTER" -> GLFW.GLFW_KEY_ENTER;
            case "BACKSPACE" -> GLFW.GLFW_KEY_BACKSPACE;
            case "INSERT" -> GLFW.GLFW_KEY_INSERT;
            case "DELETE" -> GLFW.GLFW_KEY_DELETE;
            default -> -1;
        };
    }

    private String keyName(int key) {
        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            return String.valueOf((char) ('A' + key - GLFW.GLFW_KEY_A));
        }
        if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
            return String.valueOf((char) ('0' + key - GLFW.GLFW_KEY_0));
        }
        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F12) {
            return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        }

        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "CTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "ALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_INSERT -> "INSERT";
            case GLFW.GLFW_KEY_DELETE -> "DELETE";
            default -> "KEY_" + key;
        };
    }

    private void sendMessage(String message) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.displayClientMessage(
                    Component.literal("§c[Crimson] §f" + message),
                    false
            );
        }
    }

    private void initializeVisuals() {
        // Инициализация выполняется в соответствующих визуальных классах.
    }

    private void initializeCombatModules() {
        this.killAuraModule = new KillAura();
        this.triggerBotModule = new TriggerBot();
        this.aimAssistModule = new AimAssist();
        this.chestEspModule = new ChestESP();
    }
}
