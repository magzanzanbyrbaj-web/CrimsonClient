
package ru.crimson.client.visual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.world.phys.Vec3;

import ru.crimson.client.CrimsonClientClient;

public final class FreeCamera {

    private static boolean registered;
    private static boolean active;

    private static Vec3 cameraPosition;
    private static float cameraYaw;
    private static float cameraPitch;

    private FreeCamera() {}

    public static void init() {
        if (registered) return;
        registered = true;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                stop(client);
                return;
            }

            boolean enabled = CrimsonClientClient.config != null
                    && CrimsonClientClient.config.freeCamera;

            if (enabled && !active) {
                start(client);
            } else if (!enabled && active) {
                stop(client);
            }

            if (active) {
                updateCamera(client);
            }
        });
    }

    private static void start(Minecraft client) {
        if (client.player == null) return;

        cameraPosition = client.player.position()
                .add(0.0, client.player.getEyeHeight(), 0.0);

        cameraYaw = client.player.getYRot();
        cameraPitch = client.player.getXRot();

        active = true;
    }

    private static void updateCamera(Minecraft client) {
        if (cameraPosition == null || client.player == null) {
            stop(client);
            return;
        }

        double speed = client.options.keySprint.isDown()
                ? 0.5 : 0.15;

        Vec3 forward = Vec3.directionFromRotation(
                cameraPitch,
                cameraYaw
        );

        Vec3 right = new Vec3(
                -forward.z,
                0.0,
                forward.x
        );

        Vec3 movement = Vec3.ZERO;

        if (client.options.keyUp.isDown()) {
            movement = movement.add(forward);
        }

        if (client.options.keyDown.isDown()) {
            movement = movement.subtract(forward);
        }

        if (client.options.keyLeft.isDown()) {
            movement = movement.subtract(right);
        }

        if (client.options.keyRight.isDown()) {
            movement = movement.add(right);
        }

        if (client.options.keyJump.isDown()) {
            movement = movement.add(0.0, 1.0, 0.0);
        }

        if (client.options.keyShift.isDown()) {
            movement = movement.add(0.0, -1.0, 0.0);
        }

        if (movement.lengthSqr() > 0.0) {
            cameraPosition = cameraPosition.add(
                    movement.normalize().scale(speed)
            );
        }
    }

    public static boolean isActive() {
        return active;
    }

    public static Vec3 getCameraPosition() {
        return active ? cameraPosition : null;
    }

    public static float getCameraYaw() {
        return cameraYaw;
    }

    public static float getCameraPitch() {
        return cameraPitch;
    }

    private static void stop(Minecraft client) {
        active = false;
        cameraPosition = null;
    }
}
