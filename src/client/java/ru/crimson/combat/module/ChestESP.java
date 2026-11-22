package ru.crimson.combat.module;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

@Environment(EnvType.CLIENT)
public final class ChestESP {
    private static boolean registered = false;
    private static final int MAX_RADIUS = 300;
    private static final float FILL_ALPHA = 0.28f;

    private static final RenderPipeline ESP_LINES_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation("crimsonclient/pipeline/esp_lines")
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .build();

    private static final RenderType ESP_LINES_TYPE = RenderType.create(
            "crimsonclient_esp_lines",
            RenderSetup.builder(ESP_LINES_PIPELINE).createRenderSetup(false)
    );

    private static final RenderPipeline ESP_FILL_PIPELINE = RenderPipeline.builder()
            .withLocation("crimsonclient/pipeline/esp_fill")
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .build();

    private static final RenderType ESP_FILL_TYPE = RenderType.create(
            "crimsonclient_esp_fill",
            RenderSetup.builder(ESP_FILL_PIPELINE).createRenderSetup(false)
    );

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

        int radius = MAX_RADIUS;
        double maxDistSq = (double) radius * radius;
        int chunkRadius = (radius >> 4) + 1;

        Vec3 camera = context.gameRenderer().getMainCamera().position();
        BlockPos center = mc.player.blockPosition();
        int centerChunkX = SectionPos.blockToSectionCoord(center.getX());
        int centerChunkZ = SectionPos.blockToSectionCoord(center.getZ());

        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            Matrix4f matrix = pose.last().pose();
            VertexConsumer fillBuffer = consumers.getBuffer(ESP_FILL_TYPE);
            VertexConsumer lineBuffer = consumers.getBuffer(ESP_LINES_TYPE);

            for (int cx = centerChunkX - chunkRadius; cx <= centerChunkX + chunkRadius; cx++) {
                for (int cz = centerChunkZ - chunkRadius; cz <= centerChunkZ + chunkRadius; cz++) {
                    LevelChunk chunk = mc.level.getChunkSource().getChunkNow(cx, cz);
                    if (chunk == null) continue;

                    for (BlockEntity be : chunk.getBlockEntities().values()) {
                        if (be == null || !isEnabledType(be, cfg)) continue;

                        BlockPos pos = be.getBlockPos();
                        if (pos.distSqr(center) > maxDistSq) continue;

                        int color = getColorForType(be, cfg);
                        AABB box = new AABB(pos).inflate(0.003);
                        drawFilledBox(fillBuffer, matrix, box, color, FILL_ALPHA);
                        drawOutlineBox(lineBuffer, matrix, box, color);
                    }
                }
            }
        } catch (Throwable ignored) {
            // Keep render thread safe on shader/renderer edge cases
        } finally {
            pose.popPose();
        }
    }

    private static boolean isEnabledType(BlockEntity entity, CrimsonConfig cfg) {
        if (entity instanceof TrappedChestBlockEntity) return cfg.chestEspTrapped;
        if (entity instanceof ChestBlockEntity) return cfg.chestEspNormal;
        if (entity instanceof EnderChestBlockEntity) return cfg.chestEspEnder;
        if (entity instanceof BarrelBlockEntity) return cfg.chestEspBarrel;
        if (entity instanceof ShulkerBoxBlockEntity) return cfg.chestEspShulker;
        return false;
    }

    private static int getColorForType(BlockEntity entity, CrimsonConfig cfg) {
        if (entity instanceof ShulkerBoxBlockEntity) return 0xFF55FF;
        if (entity instanceof EnderChestBlockEntity) return 0xAA00FF;
        if (entity instanceof TrappedChestBlockEntity) return 0xFF3333;
        if (entity instanceof BarrelBlockEntity) return 0xFFAA00;
        return cfg.chestEspColor;
    }

    private static void drawFilledBox(VertexConsumer out, Matrix4f matrix, AABB box, int color, float alpha) {
        float r = (float) ((color >> 16) & 0xFF) / 255.0f;
        float g = (float) ((color >> 8) & 0xFF) / 255.0f;
        float b = (float) (color & 0xFF) / 255.0f;

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        // Bottom (-Y)
        quad(out, matrix, x1, y1, z2, x2, y1, z2, x2, y1, z1, x1, y1, z1, r, g, b, alpha);
        // Top (+Y)
        quad(out, matrix, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, r, g, b, alpha);
        // North (-Z)
        quad(out, matrix, x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1, r, g, b, alpha);
        // South (+Z)
        quad(out, matrix, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, r, g, b, alpha);
        // West (-X)
        quad(out, matrix, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, r, g, b, alpha);
        // East (+X)
        quad(out, matrix, x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2, r, g, b, alpha);
    }

    private static void quad(
            VertexConsumer out,
            Matrix4f matrix,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            float r, float g, float b, float a
    ) {
        out.addVertex((Matrix4fc) matrix, x1, y1, z1).setColor(r, g, b, a);
        out.addVertex((Matrix4fc) matrix, x2, y2, z2).setColor(r, g, b, a);
        out.addVertex((Matrix4fc) matrix, x3, y3, z3).setColor(r, g, b, a);
        out.addVertex((Matrix4fc) matrix, x4, y4, z4).setColor(r, g, b, a);
    }

    private static void drawOutlineBox(VertexConsumer out, Matrix4f matrix, AABB box, int color) {
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
                .setNormal(0.0f, 1.0f, 0.0f)
                .setLineWidth(1.5f);
        out.addVertex((Matrix4fc) matrix, x2, y2, z2)
                .setColor(r, g, b, 1.0f)
                .setNormal(0.0f, 1.0f, 0.0f)
                .setLineWidth(1.5f);
    }
}
