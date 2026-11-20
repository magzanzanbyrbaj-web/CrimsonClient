package ru.crimson.client.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

public final class CustomHitbox {

    private CustomHitbox() {}

    public static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(CustomHitbox::render);
    }

    private static void render(WorldRenderContext context) {
        CrimsonConfig config = CrimsonClientClient.config;
        if (!config.customHitbox) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack matrices = context.matrixStack();
        MultiBufferSource consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        float tickDelta = context.tickCounter().getGameTimeDeltaPartialTick(true);
        Vec3 cam = context.camera().getPosition();

        matrices.pushPose();
        matrices.translate(-cam.x, -cam.y, -cam.z);

        Matrix4f matrix = matrices.last().pose();
        VertexConsumer buffer = consumers.getBuffer(RenderType.lines());

        int color = config.hitboxColor;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = 0.85f;
        float lineWidth = Math.max(1.0f, config.hitboxWidth);

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player player)) continue;
            if (player == mc.player) continue;
            if (player.isInvisible() || player.isInvisibleTo(mc.player)) continue;
            if (!player.isAlive()) continue;

            AABB box = player.getBoundingBox();
            // Interpolate position for smoothness
            double x = Mth.lerp(tickDelta, player.xOld, player.getX());
            double y = Mth.lerp(tickDelta, player.yOld, player.getY());
            double z = Mth.lerp(tickDelta, player.zOld, player.getZ());

            double dx = x - player.getX();
            double dy = y - player.getY();
            double dz = z - player.getZ();

            AABB interp = box.move(dx, dy, dz);

            drawBox(buffer, matrix, interp, r, g, b, a);
        }

        matrices.popPose();
    }

    private static void drawBox(
            VertexConsumer buffer,
            Matrix4f matrix,
            AABB box,
            float r, float g, float b, float a
    ) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        // Bottom face
        line(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        line(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        line(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        line(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        // Top face
        line(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        line(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        line(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        line(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

        // Verticals
        line(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        line(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        line(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        line(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    private static void line(
            VertexConsumer buffer,
            Matrix4f matrix,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a
    ) {
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(0, 1, 0);
        buffer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(0, 1, 0);
    }
}
