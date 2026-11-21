package ru.crimson.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

/**
 * BetterTargetEsp - более красивый ESP на цель (кольцо + glow + particles).
 * Заменяет/дополняет старый TargetEsp.
 */
public final class BetterTargetEsp {

    private static LivingEntity target;
    private static int ticksLeft;
    private static float anim;

    private BetterTargetEsp() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                target = null;
                return;
            }

            // Простая логика: последняя атакованная сущность
            // В реальном клиенте лучше через AttackEntityCallback или mixin
            if (target != null) {
                ticksLeft--;
                if (ticksLeft <= 0 || target.isRemoved() || target.isDeadOrDying()) {
                    target = null;
                }
            }
            anim = Mth.clamp(anim + (target != null ? 0.15f : -0.1f), 0f, 1f);
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(BetterTargetEsp::render);
    }

    /** Вызывать при атаке сущности */
    public static void onAttack(LivingEntity entity) {
        target = entity;
        ticksLeft = 40;
    }

    private static void render(WorldRenderContext ctx) {
        CrimsonConfig cfg = CrimsonClientClient.config;
        if (cfg == null || !cfg.targetEsp || target == null || anim <= 0.01f) return;

        Minecraft mc = Minecraft.getInstance();
        PoseStack matrices = ctx.matrixStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        float partial = ctx.tickCounter().getGameTimeDeltaPartialTick(true);
        Vec3 cam = ctx.camera().getPosition();

        double x = Mth.lerp(partial, target.xo, target.getX()) - cam.x;
        double y = Mth.lerp(partial, target.yo, target.getY()) - cam.y;
        double z = Mth.lerp(partial, target.zo, target.getZ()) - cam.z;

        float height = target.getBbHeight();
        float width = target.getBbWidth() * 0.7f * cfg.targetEspScale;

        matrices.pushPose();
        matrices.translate(x, y + height * 0.5, z);

        // Вращение
        float time = (System.currentTimeMillis() % 4000) / 4000f * 360f;
        matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(time));

        Matrix4f matrix = matrices.last().pose();

        int color = cfg.targetEspColor;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = (((color >> 24) & 0xFF) / 255f) * anim;

        // Кольцо вокруг
        int segments = 32;
        for (int i = 0; i < segments; i++) {
            float a1 = (float) (i * Math.PI * 2 / segments);
            float a2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float x1 = (float) Math.cos(a1) * width;
            float z1 = (float) Math.sin(a1) * width;
            float x2 = (float) Math.cos(a2) * width;
            float z2 = (float) Math.sin(a2) * width;

            // верхнее кольцо
            lines.addVertex(matrix, x1, height * 0.35f, z1).setColor(r, g, b, a).setNormal(0, 1, 0);
            lines.addVertex(matrix, x2, height * 0.35f, z2).setColor(r, g, b, a).setNormal(0, 1, 0);

            // нижнее
            lines.addVertex(matrix, x1, -height * 0.35f, z1).setColor(r, g, b, a * 0.6f).setNormal(0, 1, 0);
            lines.addVertex(matrix, x2, -height * 0.35f, z2).setColor(r, g, b, a * 0.6f).setNormal(0, 1, 0);
        }

        // Вертикальные линии
        for (int i = 0; i < 4; i++) {
            float ang = (float) (i * Math.PI / 2);
            float lx = (float) Math.cos(ang) * width;
            float lz = (float) Math.sin(ang) * width;
            lines.addVertex(matrix, lx, -height * 0.35f, lz).setColor(r, g, b, a * 0.8f).setNormal(0, 1, 0);
            lines.addVertex(matrix, lx, height * 0.35f, lz).setColor(r, g, b, a * 0.8f).setNormal(0, 1, 0);
        }

        matrices.popPose();
        buffers.endBatch();
    }
}
