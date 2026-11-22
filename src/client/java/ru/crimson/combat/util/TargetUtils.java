package ru.crimson.combat.util;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1569;
import net.minecraft.class_1657;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3959;

@Environment(EnvType.CLIENT)
public class TargetUtils {
    private static final class_310 mc = class_310.method_1551();

    public static class_1309 findTarget(double range, float fov, boolean players, boolean mobs, boolean throughWalls, int priority) {
        if (mc.field_1724 == null || mc.field_1687 == null) {
            return null;
        }

        List<class_1309> targets = StreamSupport.stream(mc.field_1687.method_18112().spliterator(), false)
                .filter(e -> e instanceof class_1309)
                .map(e -> (class_1309) e)
                .filter(e -> e != mc.field_1724)
                .filter(e -> e.method_5805() && !e.method_31481())
                .filter(e -> {
                    if (e instanceof class_1657) {
                        return players;
                    }
                    // Hostile + passive if mobs enabled
                    return mobs;
                })
                .filter(e -> (double) mc.field_1724.method_5739(e) <= range)
                .filter(e -> isInFov(e, fov))
                .filter(e -> throughWalls || canSee(e))
                .collect(Collectors.toList());

        if (targets.isEmpty()) {
            return null;
        }

        return switch (priority) {
            case 1 -> targets.stream().min(Comparator.comparingDouble(class_1309::method_6032)).orElse(null); // lowest health
            case 2 -> targets.stream().min(Comparator.comparingInt(class_1309::method_6096)).orElse(null); // armor
            default -> targets.stream().min(Comparator.comparingDouble(e -> mc.field_1724.method_5739(e))).orElse(null); // closest
        };
    }

    public static boolean isInFov(class_1297 entity, float fov) {
        if (mc.field_1724 == null) return false;
        if (fov >= 180.0f) return true;

        class_243 look = mc.field_1724.method_5828(1.0f);
        class_243 toEntity = entity.method_73189()
                .method_1031(0.0, entity.method_17682() / 2.0, 0.0)
                .method_1020(mc.field_1724.method_33571())
                .method_1029();

        double dot = class_3532.method_15350(look.method_1026(toEntity), -1.0, 1.0);
        double angle = Math.toDegrees(Math.acos(dot));
        return angle <= (double) fov / 2.0;
    }

    public static boolean canSee(class_1297 entity) {
        if (mc.field_1724 == null || mc.field_1687 == null) return false;

        class_243 eye = mc.field_1724.method_33571();
        class_243 target = entity.method_73189().method_1031(0.0, entity.method_17682() * 0.5, 0.0);

        return mc.field_1687.method_17742(new class_3959(
                eye, target,
                class_3959.class_3960.field_17558,
                class_3959.class_242.field_1348,
                mc.field_1724
        )).method_17783() == class_239.class_240.field_1333;
    }

    public static float[] getRotations(class_1297 entity) {
        if (mc.field_1724 == null) {
            return new float[]{0.0f, 0.0f};
        }

        class_243 eye = mc.field_1724.method_33571();
        class_243 target = entity.method_73189().method_1031(0.0, entity.method_17682() * 0.85, 0.0);

        double diffX = target.field_1352 - eye.field_1352;
        double diffY = target.field_1351 - eye.field_1351;
        double diffZ = target.field_1350 - eye.field_1350;

        double distance = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, distance)));

        return new float[]{yaw, pitch};
    }

    public static float smoothAngle(float current, float target, float speed) {
        float factor = class_3532.method_15363(speed, 0.0f, 1.0f);
        return current + class_3532.method_15393(target - current) * factor;
    }

    public static boolean isCriticalHitReady() {
        if (mc.field_1724 == null) return false;
        return !mc.field_1724.method_24828()
                && mc.field_1724.field_6017 > 0.0
                && !mc.field_1724.method_5799()
                && !mc.field_1724.method_5771();
    }
}
