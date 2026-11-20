package ru.crimson.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.client.gui.CrimsonMenuScreen;
import ru.crimson.client.hud.HudRenderer;

import ru.crimson.client.visual.TargetEsp;
import ru.crimson.client.visual.CustomHitbox;
import ru.crimson.client.visual.BlockOverlay;
import ru.crimson.client.visual.JumpCircle;
import ru.crimson.client.visual.CustomSky;

public class CrimsonClientClient implements ClientModInitializer {

    public static KeyMapping openMenuKey;
    public static CrimsonConfig config;

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

        HudRenderer.init();

        TargetEsp.init();
        CustomHitbox.init();
        BlockOverlay.init();
        JumpCircle.init();
        CustomSky.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new CrimsonMenuScreen());
                }
            }

            if (config.fullbright && client.options != null) {
                client.options.gamma().set(16.0);
            }
        });

        CrimsonClient.LOGGER.info(
                "Crimson Client initialized. Press Right Shift."
        );
    }
}
