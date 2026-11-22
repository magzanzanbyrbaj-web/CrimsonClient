
package ru.crimson.client.player;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;

public final class ChestStealer {

    private static boolean registered;
    private static boolean enabled;

    private static int occupiedSlots;
    private static int totalItems;
    private static String status = "Контейнер закрыт";

    private ChestStealer() {}

    public static void init() {
        if (registered) return;
        registered = true;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                reset();
                return;
            }

            if (!enabled) {
                status = "Выключен";
                return;
            }

            AbstractContainerMenu menu =
                    client.player.containerMenu;

            if (!(menu instanceof ChestMenu)) {
                reset();
                return;
            }

            occupiedSlots = 0;
            totalItems = 0;

            int containerSlots = menu.slots.size()
                    - client.player.getInventory().getContainerSize();

            containerSlots = Math.max(
                    0,
                    Math.min(containerSlots, menu.slots.size())
            );

            for (int i = 0; i < containerSlots; i++) {
                ItemStack stack = menu.getSlot(i).getItem();

                if (stack.isEmpty()) continue;

                occupiedSlots++;
                totalItems += stack.getCount();
            }

            status = "Контейнер открыт";
        });
    }

    private static void reset() {
        occupiedSlots = 0;
        totalItems = 0;
        status = "Контейнер закрыт";
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!value) reset();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static int getOccupiedSlots() {
        return occupiedSlots;
    }

    public static int getTotalItems() {
        return totalItems;
    }

    public static String getStatus() {
        return status;
    }
}
