
package ru.crimson.client.movement;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public final class Scaffold {

    private static boolean registered;

    private static boolean enabled;
    private static String selectedBlock = "Нет блока";
    private static boolean lookingAtBlock;

    private Scaffold() {}

    public static void init() {
        if (registered) return;
        registered = true;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                enabled = false;
                selectedBlock = "Нет блока";
                lookingAtBlock = false;
                return;
            }

            ItemStack stack = client.player.getMainHandItem();

            if (stack.getItem() instanceof BlockItem blockItem) {
                selectedBlock = blockItem.getBlock()
                        .getName()
                        .getString();
            } else {
                selectedBlock = "Нет блока";
            }

            lookingAtBlock =
                    client.hitResult instanceof BlockHitResult;
        });
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static String getSelectedBlock() {
        return selectedBlock;
    }

    public static boolean isLookingAtBlock() {
        return lookingAtBlock;
    }

    public static String getStatus() {
        if (!enabled) return "Выключен";
        if ("Нет блока".equals(selectedBlock)) {
            return "Возьми блок в руку";
        }
        if (!lookingAtBlock) return "Наведи на блок";
        return "Готов к строительству";
    }
}
