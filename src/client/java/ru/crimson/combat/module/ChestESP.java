package ru.crimson.combat.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
        Minecraft mc = Minecraft.getInstance();
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.chestEsp || mc.level == null || mc.player == null) {
            return;
        }

        PoseStack pose = context.matrices();
        MultiBufferSource consumers = context.consumers();
        if (pose == null || consumers == null) return;

        Vec3 camera = context.gameRenderer().getMainCamera().getPosition();
        BlockPos center = mc.player.blockPosition();

        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            Matrix4f matrix = pose.last().pose();
            VertexConsumer buffer = consumers.getBuffer(RenderType.lines());

            for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
                for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                    for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        BlockEntity be = mc.level.getBlockEntity(pos);
                        if (be == null || !isEnabledType(be, cfg)) continue;

                        AABB box = new AABB(pos).inflate(0.002);
                        drawBox(buffer, matrix, box, cfg.chestEspColor);
                    }
                }
            }
        } finally {
            pose.popPose();
        }
    }

    private static boolean isEnabledType(BlockEntity entity, CrimsonConfig cfg) {
        if (entity instanceof TrappedChestBlockEntity) return cfg.chestEspTrapped; // Trapped Chest
        if (entity instanceof ChestBlockEntity) return cfg.chestEspNormal;         // Chest
        if (entity instanceof EnderChestBlockEntity) return cfg.chestEspEnder;     // Ender Chest
        if (entity instanceof BarrelBlockEntity) return cfg.chestEspBarrel;        // Barrel
        if (entity instanceof ShulkerBoxBlockEntity) return cfg.chestEspShulker;   // Shulker
        return false;
    }

    private static void drawBox(VertexConsumer out, Matrix4f matrix, AABB box, int color) {
        float r = (float) ((color >> 16) & 0xFF) / 255.0f;
        float g = (float) ((color >> 8) & 0xFF) / 255.0f;
        float b = (float) (color & 0xFF) / 255.0f;

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

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

    private static void line(VertexConsumer out, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b) {
        out.addVertex((Matrix4fc) matrix, x1, y1, z1)
                .setColor(r, g, b, 1.0f)
                .setLineWidth(1.5f)
                .setNormal(0.0f, 1.0f, 0.0f);
        out.addVertex((Matrix4fc) matrix, x2, y2, z2)
                .setColor(r, g, b, 1.0f)
                .setLineWidth(1.5f)
                .setNormal(0.0f, 1.0f, 0.0f);
    }
}
