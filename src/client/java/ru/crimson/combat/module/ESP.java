package ru.crimson.combat.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

public final class ESP {
    private static boolean registered;
    public ESP() {
        if (!registered) {
            registered = true;
            WorldRenderEvents.AFTER_ENTITIES.register(ESP::renderWorld);
        }
    }

    public void onTick() {
        // Отрисовка выполняется в WorldRenderEvents.AFTER_ENTITIES.
    }

    private static void renderWorld(WorldRenderContext context) {
        CrimsonConfig cfg = CrimsonClientClient.config;
        Minecraft mc = Minecraft.getInstance();
        if (cfg == null || !cfg.esp || mc.level == null || mc.player == null) return;

        PoseStack pose = context.matrices();
        MultiBufferSource consumers = context.consumers();
        if (pose == null || consumers == null) return;

        Vec3 camera = context.gameRenderer().getMainCamera().position();
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            Matrix4f matrix = pose.last().pose();
            VertexConsumer buffer = consumers.getBuffer(RenderTypes.lines());

            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof LivingEntity living) || !living.isAlive()) continue;
                if (living == mc.player || living.isInvisible() || living.isInvisibleTo(mc.player)) continue;

                boolean player = living instanceof Player;
                if (player && !cfg.espPlayers) continue;
                if (!player && !cfg.espMobs) continue;

                AABB box = living.getBoundingBox().inflate(0.025);
                float red = player ? 1.0f : 0.25f;
                float green = player ? 0.20f : 0.90f;
                float blue = player ? 0.28f : 0.45f;
                drawBox(buffer, matrix, box, red, green, blue);
            }
        } finally {
            pose.popPose();
        }
    }

    private static void drawBox(VertexConsumer out, Matrix4f m, AABB b,
                                float r, float g, float bl) {
        float x1=(float)b.minX, y1=(float)b.minY, z1=(float)b.minZ;
        float x2=(float)b.maxX, y2=(float)b.maxY, z2=(float)b.maxZ;
        line(out,m,x1,y1,z1,x2,y1,z1,r,g,bl);
        line(out,m,x2,y1,z1,x2,y1,z2,r,g,bl);
        line(out,m,x2,y1,z2,x1,y1,z2,r,g,bl);
        line(out,m,x1,y1,z2,x1,y1,z1,r,g,bl);
        line(out,m,x1,y2,z1,x2,y2,z1,r,g,bl);
        line(out,m,x2,y2,z1,x2,y2,z2,r,g,bl);
        line(out,m,x2,y2,z2,x1,y2,z2,r,g,bl);
        line(out,m,x1,y2,z2,x1,y2,z1,r,g,bl);
        line(out,m,x1,y1,z1,x1,y2,z1,r,g,bl);
        line(out,m,x2,y1,z1,x2,y2,z1,r,g,bl);
        line(out,m,x2,y1,z2,x2,y2,z2,r,g,bl);
        line(out,m,x1,y1,z2,x1,y2,z2,r,g,bl);
    }

    private static void line(VertexConsumer out, Matrix4f m,
                             float x1,float y1,float z1,float x2,float y2,float z2,
                             float r,float g,float b) {
        out.addVertex(m,x1,y1,z1).setColor(r,g,b,1.0f).setNormal(0,1,0);
        out.addVertex(m,x2,y2,z2).setColor(r,g,b,1.0f).setNormal(0,1,0);
    }
}
