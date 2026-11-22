package ru.crimson.combat.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Environment(EnvType.CLIENT)
public class TargetUtils {
    private static final Minecraft mc = Minecraft.getInstance();

    public static LivingEntity findTarget(double range, float fov, boolean players, boolean mobs, boolean throughWalls, int priority) {
        if (mc.player == null || mc.level == null) {
            return null;
        }

        List<LivingEntity> targets = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false)
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(e -> e != mc.player)
                .filter(e -> e.isAlive() && !e.isSpectator())
                .filter(e -> {
                    if (e instanceof Player) {
                        return players;
                    }
                    // Hostile + passive if mobs enabled
                    return mobs;
                })
                .filter(e -> (double) mc.player.distanceTo(e) <= range)
                .filter(e -> isInFov(e, fov))
                .filter(e -> throughWalls || canSee(e))
                .collect(Collectors.toList());

        if (targets.isEmpty()) {
            return null;
        }

        return switch (priority) {
            case 1 -> targets.stream().min(Comparator.comparingDouble(LivingEntity::getHealth)).orElse(null); // lowest health
            case 2 -> targets.stream().min(Comparator.comparingInt(LivingEntity::getArmorValue)).orElse(null); // armor
            default -> targets.stream().min(Comparator.comparingDouble(e -> mc.player.distanceTo(e))).orElse(null); // closest
        };
    }

    public static boolean isInFov(Entity entity, float fov) {
        if (mc.player == null) return false;
        if (fov >= 180.0f) return true;

        Vec3 look = mc.player.getViewVector(1.0f);
        Vec3 toEntity = entity.position()
                .add(0.0, entity.getBbHeight() / 2.0, 0.0)
                .subtract(mc.player.getEyePosition())
                .normalize();

        double dot = Mth.clamp(look.dot(toEntity), -1.0, 1.0);
        double angle = Math.toDegrees(Math.acos(dot));
        return angle <= (double) fov / 2.0;
    }

    public static boolean canSee(Entity entity) {
        if (mc.player == null || mc.level == null) return false;

        Vec3 eye = mc.player.getEyePosition();
        Vec3 target = entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0);

        return mc.level.clip(new ClipContext(
                eye, target,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mc.player
        )).getType() == HitResult.Type.MISS;
    }

    public static float[] getRotations(Entity entity) {
        if (mc.player == null) {
            return new float[]{0.0f, 0.0f};
        }

        Vec3 eye = mc.player.getEyePosition();
        Vec3 target = entity.position().add(0.0, entity.getBbHeight() * 0.85, 0.0);

        double diffX = target.x - eye.x;
        double diffY = target.y - eye.y;
        double diffZ = target.z - eye.z;

        double distance = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, distance)));

        return new float[]{yaw, pitch};
    }

    public static float smoothAngle(float current, float target, float speed) {
        float factor = Mth.clamp(speed, 0.0f, 1.0f);
        return current + Mth.wrapDegrees(target - current) * factor;
    }

    public static boolean isCriticalHitReady() {
        if (mc.player == null) return false;
        return !mc.player.onGround()
                && mc.player.fallDistance > 0.0
                && !mc.player.isInWater()
                && !mc.player.isPassenger();
    }
}
