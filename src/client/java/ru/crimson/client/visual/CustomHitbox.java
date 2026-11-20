
package ru.crimson.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
        if (config == null || !config.customHitbox) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3 cam = context.gameRenderer().getMainCamera().position();

        matrices.pushPose();
        try {
            matrices.translate(-cam.x, -cam.y, -cam.z);

            Matrix4f matrix = matrices.last().pose();
            VertexConsumer buffer = consumers.getBuffer(RenderTypes.lines());

            int color = config.hitboxColor;
            float r = ((color >> 16) & 255) / 255f;
            float g = ((color >> 8) & 255) / 255f;
            float b = (color & 255) / 255f;

            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof Player player)) continue;
                if (player == mc.player) continue;
                if (!player.isAlive() || player.isInvisible()
                        || player.isInvisibleTo(mc.player)) continue;

                drawBox(buffer, matrix, player.getBoundingBox(),
                        r, g, b, Math.max(1.0f, config.hitboxWidth));
            }
        } finally {
            matrices.popPose();
        }
    }

    private static void drawBox(VertexConsumer buffer, Matrix4f matrix,
                                AABB box, float r, float g, float b,
                                float width) {
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        line(buffer, matrix, x1,y1,z1, x2,y1,z1, r,g,b,width);
        line(buffer, matrix, x2,y1,z1, x2,y1,z2, r,g,b,width);
        line(buffer, matrix, x2,y1,z2, x1,y1,z2, r,g,b,width);
        line(buffer, matrix, x1,y1,z2, x1,y1,z1, r,g,b,width);

        line(buffer, matrix, x1,y2,z1, x2,y2,z1, r,g,b,width);
        line(buffer, matrix, x2,y2,z1, x2,y2,z2, r,g,b,width);
        line(buffer, matrix, x2,y2,z2, x1,y2,z2, r,g,b,width);
        line(buffer, matrix, x1,y2,z2, x1,y2,z1, r,g,b,width);

        line(buffer, matrix, x1,y1,z1, x1,y2,z1, r,g,b,width);
        line(buffer, matrix, x2,y1,z1, x2,y2,z1, r,g,b,width);
        line(buffer, matrix, x2,y1,z2, x2,y2,z2, r,g,b,width);
        line(buffer, matrix, x1,y1,z2, x1,y2,z2, r,g,b,width);
    }

    private static void line(VertexConsumer buffer, Matrix4f matrix,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float r, float g, float b, float width) {
        buffer.addVertex(matrix, x1, y1, z1)
                .setColor(r, g, b, 0.9f)
                .setNormal(0, 1, 0);
        buffer.setLineWidth(width);

        buffer.addVertex(matrix, x2, y2, z2)
                .setColor(r, g, b, 0.9f)
                .setNormal(0, 1, 0);
        buffer.setLineWidth(width);
    }
}
