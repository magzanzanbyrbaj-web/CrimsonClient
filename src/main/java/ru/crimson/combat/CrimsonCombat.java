package ru.crimson.combat;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import ru.crimson.combat.gui.CombatMenuScreen;
import ru.crimson.combat.module.*;

public class CrimsonCombat implements ClientModInitializer {
    public static KeyBinding openMenuKey;
    public static CombatConfig config = new CombatConfig();

    public static KillAura killAura;
    public static TriggerBot triggerBot;
    public static AimAssist aimAssist;
    public static ESP esp;

    @Override
    public void onInitializeClient() {
        config.load();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.crimson-combat.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.crimson-combat"
        ));

        killAura = new KillAura();
        triggerBot = new TriggerBot();
        aimAssist = new AimAssist();
        esp = new ESP();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openMenuKey.wasPressed() && client.currentScreen == null) {
                client.setScreen(new CombatMenuScreen());
            }

            if (client.player == null || client.world == null) return;

            killAura.onTick(client);
            triggerBot.onTick(client);
            aimAssist.onTick(client);
            esp.onTick(client);
        });
    }
}
