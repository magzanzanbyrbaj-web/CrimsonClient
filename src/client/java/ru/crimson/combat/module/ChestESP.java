package ru.crimson.combat.module;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.class_12249;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2611;
import net.minecraft.class_2627;
import net.minecraft.class_2646;
import net.minecraft.class_310;
import net.minecraft.class_3719;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

@Environment(EnvType.CLIENT)
public final class ChestESP {
    private static boolean registered = false;
    private static final int SCAN_RADIUS = 10;

    public ChestESP() {
        if (registered) return;
        registered = true;
        WorldRenderEvents.AFTER_ENTITIES.register(ChestESP::render);
    }

    public void onTick() {
        // Empty - rendering is event-driven
    }

    private static void render(WorldRenderContext context) {
        class_310 mc = class_310.method_1551();
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.chestEsp || mc.field_1687 == null || mc.field_1724 == null) {
            return;
        }

        class_4587 pose = context.matrices();
        class_4597 consumers = context.consumers();
        if (pose == null || consumers == null) return;

        class_243 camera = context.gameRenderer().method_19418().method_71156();
        class_2338 center = mc.field_1724.method_24515();

        pose.method_22903();
        try {
            pose.method_22904(-camera.field_1352, -camera.field_1351, -camera.field_1350);
            Matrix4f matrix = pose.method_23760().method_23761();
            class_4588 buffer = consumers.method_73477(class_12249.method_76015());

            for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
                for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                    for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                        class_2338 pos = center.method_10069(x, y, z);
                        class_2586 be = mc.field_1687.method_8321(pos);
                        if (be == null || !isEnabledType(be, cfg)) continue;

                        class_238 box = new class_238(pos).method_1014(0.002);
                        drawBox(buffer, matrix, box, cfg.chestEspColor);
                    }
                }
            }
        } finally {
            pose.method_22909();
        }
    }

    private static boolean isEnabledType(class_2586 entity, CrimsonConfig cfg) {
        if (entity instanceof class_2646) return cfg.chestEspTrapped;      // Trapped Chest
        if (entity instanceof class_2595) return cfg.chestEspNormal;       // Chest
        if (entity instanceof class_2611) return cfg.chestEspEnder;        // Ender Chest
        if (entity instanceof class_3719) return cfg.chestEspBarrel;       // Barrel
        if (entity instanceof class_2627) return cfg.chestEspShulker;      // Shulker
        return false;
    }

    private static void drawBox(class_4588 out, Matrix4f matrix, class_238 box, int color) {
        float r = (float) ((color >> 16) & 0xFF) / 255.0f;
        float g = (float) ((color >> 8) & 0xFF) / 255.0f;
        float b = (float) (color & 0xFF) / 255.0f;

        float x1 = (float) box.field_1323;
        float y1 = (float) box.field_1322;
        float z1 = (float) box.field_1321;
        float x2 = (float) box.field_1320;
        float y2 = (float) box.field_1325;
        float z2 = (float) box.field_1324;

        // Bottom
        line(out, matrix, x1, y1, z1, x2, y1, z1, r, g, b);
        line(out, matrix, x2, y1, z1, x2, y1, z2, r, g, b);
        line(out, matrix, x2, y1, z2, x1, y1, z2, r, g, b);
        line(out, matrix, x1, y1, z2, x1, y1, z1, r, g, b);

        // Top
        line(out, matrix, x1, y2, z1, x2, y2, z1, r, g, b);
        line(out, matrix, x2, y2, z1, x2, y2, z2, r, g, b);
        line(out, matrix, x2, y2, z2, x1, y2, z2, r, g, b);
        line(out, matrix, x1, y2, z2, x1, y2, z1, r, g, b);

        // Verticals
        line(out, matrix, x1, y1, z1, x1, y2, z1, r, g, b);
        line(out, matrix, x2, y1, z1, x2, y2, z1, r, g, b);
        line(out, matrix, x2, y1, z2, x2, y2, z2, r, g, b);
        line(out, matrix, x1, y1, z2, x1, y2, z2, r, g, b);
    }

    private static void line(class_4588 out, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b) {
        out.method_22918((Matrix4fc) matrix, x1, y1, z1)
                .method_22915(r, g, b, 1.0f)
                .method_75298(1.5f)
                .method_22914(0.0f, 1.0f, 0.0f);
        out.method_22918((Matrix4fc) matrix, x2, y2, z2)
                .method_22915(r, g, b, 1.0f)
                .method_75298(1.5f)
                .method_22914(0.0f, 1.0f, 0.0f);
    }
}
