
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

public class CrimsonClientClient implements ClientModInitializer {

    public static KeyMapping openMenuKey;
    public static CrimsonConfig config;

    @Override
    public void onInitializeClient() {

        // Загружаем конфигурацию
        config = CrimsonConfig.load();

        // Клавиша открытия меню — Right Shift
        openMenuKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.crimsonclient.open_menu",
                        GLFW.GLFW_KEY_RIGHT_SHIFT,
                        KeyMapping.Category.MISC
                )
        );

        // HUD
        HudRenderer.init();

        // Визуальные модули
        BetterTargetEsp.init();
        ChinaHat.init();
        Trails.init();
        CustomHitbox.init();
        BlockOverlay.init();
        JumpCircle.init();
        CustomSky.init();

        // Эффекты
        HitSound.init();
        CriticalEffects.init();

        // Дополнительные модули
        ReachDisplay.init();
        PearlTracker.init();

        // Обработка клавиш и обновление клиента
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
        });

        CrimsonClient.LOGGER.info(
                "Crimson Client initialized. Press Right Shift."
        );
    }
}
