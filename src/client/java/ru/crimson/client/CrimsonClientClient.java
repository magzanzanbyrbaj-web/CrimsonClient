package ru.crimson.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.client.gui.CrimsonMenuScreen;
import ru.crimson.client.gui.HudEditorScreen;
import ru.crimson.client.hud.HudRenderer;

import ru.crimson.client.visual.BetterTargetEsp;
import ru.crimson.client.visual.ChinaHat;
import ru.crimson.client.visual.Trails;
import ru.crimson.client.visual.CustomHitbox;
import ru.crimson.client.visual.BlockOverlay;
import ru.crimson.client.visual.JumpCircle;
import ru.crimson.client.visual.CustomSky;
import ru.crimson.client.visual.HitSound;
import ru.crimson.client.visual.CriticalEffects;
import ru.crimson.client.visual.ReachDisplay;
import ru.crimson.client.visual.PearlTracker;

import ru.crimson.combat.module.KillAura;
import ru.crimson.combat.module.TriggerBot;
import ru.crimson.combat.module.AimAssist;
import ru.crimson.combat.module.ESP;

public class CrimsonClientClient implements ClientModInitializer {

    public static KeyMapping openMenuKey;
    public static KeyMapping hudEditorKey;

    public static CrimsonConfig config;

    public static KillAura killAura;
    public static TriggerBot triggerBot;
    public static AimAssist aimAssist;
    public static ESP esp;

    private static final Map<Integer, Boolean> previousKeyStates =
            new HashMap<>();

    @Override
    public void onInitializeClient() {
        config = CrimsonConfig.load();

        openMenuKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.crimsonclient.open_menu",
                        GLFW.GLFW_KEY_RIGHT_SHIFT,
                        KeyMapping.Category.MISC
                )
        );

        hudEditorKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.crimsonclient.hud_editor",
                        GLFW.GLFW_KEY_F8,
                        KeyMapping.Category.MISC
                )
        );

        HudRenderer.init();

        BetterTargetEsp.init();
        ChinaHat.init();
        Trails.init();
        CustomHitbox.init();
        BlockOverlay.init();
        JumpCircle.init();
        CustomSky.init();

        HitSound.init();
        CriticalEffects.init();

        ReachDisplay.init();
        PearlTracker.init();

        killAura = new KillAura();
        triggerBot = new TriggerBot();
        aimAssist = new AimAssist();
        esp = new ESP();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                return;
            }

            while (openMenuKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new CrimsonMenuScreen());
                }
            }

            while (hudEditorKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new HudEditorScreen());
                }
            }

            handleCustomBinds(client.getWindow().handle());

            if (config.fullbright && client.options != null) {
                client.options.gamma().set(16.0);
            }

            killAura.onTick();
            triggerBot.onTick();
            aimAssist.onTick();
            esp.onTick();
        });

        CrimsonClient.LOGGER.info(
                "Crimson Client initialized. Press Right Shift for menu, F8 for HUD editor."
        );
    }

    private void handleCustomBinds(long window) {
        if (config == null) return;

        checkBind(window, config.bindKillAura, () -> {
            config.killAura = !config.killAura;
            config.save();
        });

        checkBind(window, config.bindTriggerBot, () -> {
            config.triggerBot = !config.triggerBot;
            config.save();
        });

        checkBind(window, config.bindAimAssist, () -> {
            config.aimAssist = !config.aimAssist;
            config.save();
        });

        checkBind(window, config.bindESP, () -> {
            config.esp = !config.esp;
            config.save();
        });

        checkBind(window, config.bindTargetHUD, () -> {
            config.targetHud = !config.targetHud;
            config.save();
        });

        checkBind(window, config.bindArmorHUD, () -> {
            config.armorHud = !config.armorHud;
            config.save();
        });

        checkBind(window, config.bindTotemCounter, () -> {
            config.totemCounter = !config.totemCounter;
            config.save();
        });

        checkBind(window, config.bindWatermark, () -> {
            config.watermark = !config.watermark;
            config.save();
        });

        checkBind(window, config.bindKeystrokes, () -> {
            config.keystrokes = !config.keystrokes;
            config.save();
        });

        checkBind(window, config.bindModuleList, () -> {
            config.moduleList = !config.moduleList;
            config.save();
        });

        checkBind(window, config.bindInventoryHUD, () -> {
            config.inventoryHud = !config.inventoryHud;
            config.save();
        });

        checkBind(window, config.bindPotionHUD, () -> {
            config.potionHud = !config.potionHud;
            config.save();
        });

        checkBind(window, config.bindDurabilityHUD, () -> {
            config.durabilityHud = !config.durabilityHud;
            config.save();
        });
    }

    private void checkBind(
            long window,
            int key,
            Runnable action
    ) {
        if (key <= 0 || key >= GLFW.GLFW_KEY_LAST) {
            return;
        }

        boolean down = GLFW.glfwGetKey(window, key)
                == GLFW.GLFW_PRESS;

        boolean wasDown = previousKeyStates.getOrDefault(key, false);

        if (down && !wasDown) {
            action.run();
        }

        previousKeyStates.put(key, down);
    }
}
