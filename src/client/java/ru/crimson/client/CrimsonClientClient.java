
package ru.crimson.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.client.gui.CrimsonMenuScreen;
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
    public static CrimsonConfig config;

    // Combat modules
    public static KillAura killAura;
    public static TriggerBot triggerBot;
    public static AimAssist aimAssist;
    public static ESP esp;

    @Override
    public void onInitializeClient() {

        // Load configuration
        config = CrimsonConfig.load();

        // Register menu key
        openMenuKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.crimsonclient.open_menu",
                        GLFW.GLFW_KEY_RIGHT_SHIFT,
                        KeyMapping.Category.MISC
                )
        );

        // Initialize HUD
        HudRenderer.init();

        // Visual modules
        BetterTargetEsp.init();
        ChinaHat.init();
        Trails.init();
        CustomHitbox.init();
        BlockOverlay.init();
        JumpCircle.init();
        CustomSky.init();

        // Effects
        HitSound.init();
        CriticalEffects.init();

        // Additional modules
        ReachDisplay.init();
        PearlTracker.init();

        // Initialize combat modules
        killAura = new KillAura();
        triggerBot = new TriggerBot();
        aimAssist = new AimAssist();
        esp = new ESP();

        // Client tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (openMenuKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new CrimsonMenuScreen());
                }
            }

            // FullBright
            if (client.options != null && config != null) {
                if (config.fullbright) {
                    client.options.gamma().set(16.0);
                }
            }

            // Combat modules
            if (client.player == null || client.level == null) {
                return;
            }

            killAura.onTick();
            triggerBot.onTick();
            aimAssist.onTick();
            esp.onTick();
        });

        CrimsonClient.LOGGER.info(
                "Crimson Client initialized. Press Right Shift."
        );
    }
}
