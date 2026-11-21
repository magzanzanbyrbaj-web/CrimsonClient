package ru.crimson.combat.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TargetUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static LivingEntity findTarget(double range, float fov, boolean players, boolean mobs, boolean throughWalls, int priority) {
        if (mc.player == null || mc.world == null) return null;

        List<LivingEntity> targets = StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(e -> e != mc.player && e.isAlive() && !e.isRemoved())
                .filter(e -> {
                    if (e instanceof PlayerEntity) return players;
                    if (e instanceof HostileEntity || e.getType().getSpawnGroup().isPeaceful() == false) return mobs;
                    return mobs;
                })
                .filter(e -> mc.player.distanceTo(e) <= range)
                .filter(e -> isInFov(e, fov))
                .filter(e -> throughWalls || canSee(e))
                .collect(Collectors.toList());

        if (targets.isEmpty()) return null;

        return switch (priority) {
            case 1 -> targets.stream().min(Comparator.comparingDouble(LivingEntity::getHealth)).orElse(null);
            case 2 -> targets.stream().min(Comparator.comparingDouble(e -> e.getArmor())).orElse(null);
            default -> targets.stream().min(Comparator.comparingDouble(e -> mc.player.distanceTo(e))).orElse(null);
        };
    }

    public static boolean isInFov(Entity entity, float fov) {
        if (fov >= 180) return true;
        Vec3d look = mc.player.getRotationVec(1.0f);
        Vec3d toEntity = entity.getPos().add(0, entity.getHeight() / 2, 0).subtract(mc.player.getEyePos()).normalize();
        double angle = Math.toDegrees(Math.acos(look.dotProduct(toEntity)));
        return angle <= fov / 2.0;
    }

    public static boolean canSee(Entity entity) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d target = entity.getPos().add(0, entity.getHeight() * 0.5, 0);
        return mc.world.raycast(new RaycastContext(eye, target, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS;
    }

    public static float[] getRotations(Entity entity) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d target = entity.getPos().add(0, entity.getHeight() * 0.85, 0);
        double diffX = target.x - eye.x;
        double diffY = target.y - eye.y;
        double diffZ = target.z - eye.z;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));
        return new float[]{yaw, pitch};
    }
}
