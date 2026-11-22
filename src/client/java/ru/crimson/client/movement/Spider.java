
package ru.crimson.client.movement;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import ru.crimson.client.CrimsonClientClient;

public final class Spider {

    private static boolean registered;

    private Spider() {}

    public static void init() {
        if (registered) return;
        registered = true;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                return;
            }

            if (CrimsonClientClient.config == null
                    || !CrimsonClientClient.config.spider) {
                return;
            }

            if (!client.player.horizontalCollision) {
                return;
            }

            if (client.player.isInWater()
                    || client.player.isInLava()
                    || client.player.isFallFlying()) {
                return;
            }

            Vec3 velocity = client.player.getDeltaMovement();

            if (velocity.y < 0.18) {
                client.player.setDeltaMovement(
                        velocity.x,
                        0.18,
                        velocity.z
                );

                client.player.fallDistance = 0.0f;
            }
        });
    }
}
