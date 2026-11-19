package ru.crimson.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import ru.crimson.client.config.CrimsonConfig;
import ru.crimson.client.gui.CrimsonMenuScreen;
import ru.crimson.client.hud.HudRenderer;

public class CrimsonClientClient implements ClientModInitializer {

	public static KeyMapping openMenuKey;
	public static CrimsonConfig config;

	@Override
	public void onInitializeClient() {
		config = CrimsonConfig.load();

		openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.crimsonclient.open_menu",
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				"category.crimsonclient"
		));

		HudRenderer.init();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openMenuKey.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new CrimsonMenuScreen());
				}
			}

			// Apply fullbright every tick if enabled
			if (config.fullbright && client.options != null) {
				client.options.gamma().set(16.0);
			}
		});

		CrimsonClient.LOGGER.info("Crimson Client client-side initialized. Press Right Shift for menu. HUDs ready.");
	}
}
