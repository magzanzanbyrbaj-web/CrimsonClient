
package ru.crimson.combat.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
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

import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

public final class ChestESP {

    private static boolean registered;

    private static final int SCAN_RADIUS = 8;

    public ChestESP() {
        if (registered) return;

        registered = true;
        WorldRenderEvents.AFTER_ENTITIES.register(
                ChestESP::render
        );
    }

    public void onTick() {
        // Rendering is handled by WorldRenderEvents.
    }

    private static void render(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.chestEsp
                || mc.level == null || mc.player == null) {
            return;
        }

        PoseStack pose = context.matrices();
        MultiBufferSource consumers = context.consumers();

        if (pose == null || consumers == null) return;

        Vec3 camera = context.gameRenderer()
                .getMainCamera().position();

        BlockPos center = mc.player.blockPosition();

        pose.pushPose();

        try {
            pose.translate(-camera.x, -camera.y, -camera.z);

            Matrix4f matrix = pose.last().pose();
            VertexConsumer buffer =
                    consumers.getBuffer(RenderTypes.lines());

            int radius = SCAN_RADIUS;

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {

                        BlockPos pos = center.offset(x, y, z);

                        BlockEntity blockEntity =
                                mc.level.getBlockEntity(pos);

                        if (blockEntity == null) continue;

                        if (!isEnabledType(blockEntity, cfg)) {
                            continue;
                        }

                        AABB box = new AABB(pos)
                                .inflate(0.002);

                        drawBox(
                                buffer,
                                matrix,
                                box,
                                cfg.chestEspColor
                        );
                    }
                }
            }
        } finally {
            pose.popPose();
        }
    }

    private static boolean isEnabledType(
            BlockEntity entity,
            CrimsonConfig cfg
    ) {
        if (entity instanceof TrappedChestBlockEntity) {
            return cfg.chestEspTrapped;
        }

        if (entity instanceof ChestBlockEntity) {
            return cfg.chestEspNormal;
        }

        if (entity instanceof EnderChestBlockEntity) {
            return cfg.chestEspEnder;
        }

        if (entity instanceof BarrelBlockEntity) {
            return cfg.chestEspBarrel;
        }

        if (entity instanceof ShulkerBoxBlockEntity) {
            return cfg.chestEspShulker;
        }

        return false;
    }

    private static void drawBox(
            VertexConsumer out,
            Matrix4f matrix,
            AABB box,
            int color
    ) {
        float red = ((color >> 16) & 255) / 255.0f;
        float green = ((color >> 8) & 255) / 255.0f;
        float blue = (color & 255) / 255.0f;

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;

        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        line(out, matrix, x1,y1,z1, x2,y1,z1, red,green,blue);
        line(out, matrix, x2,y1,z1, x2,y1,z2, red,green,blue);
        line(out, matrix, x2,y1,z2, x1,y1,z2, red,green,blue);
        line(out, matrix, x1,y1,z2, x1,y1,z1, red,green,blue);

        line(out, matrix, x1,y2,z1, x2,y2,z1, red,green,blue);
        line(out, matrix, x2,y2,z1, x2,y2,z2, red,green,blue);
        line(out, matrix, x2,y2,z2, x1,y2,z2, red,green,blue);
        line(out, matrix, x1,y2,z2, x1,y2,z1, red,green,blue);

        line(out, matrix, x1,y1,z1, x1,y2,z1, red,green,blue);
        line(out, matrix, x2,y1,z1, x2,y2,z1, red,green,blue);
        line(out, matrix, x2,y1,z2, x2,y2,z2, red,green,blue);
        line(out, matrix, x1,y1,z2, x1,y2,z2, red,green,blue);
    }

    private static void line(
            VertexConsumer out,
            Matrix4f matrix,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float red, float green, float blue
    ) {
        out.addVertex(matrix, x1, y1, z1)
                .setColor(red, green, blue, 1.0f)
                .setLineWidth(1.5f)
                .setNormal(0, 1, 0);

        out.addVertex(matrix, x2, y2, z2)
                .setColor(red, green, blue, 1.0f)
                .setLineWidth(1.5f)
                .setNormal(0, 1, 0);
    }
}
