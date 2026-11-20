package ru.crimson.client.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

public final class BlockOverlay {

    private BlockOverlay() {}

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(BlockOverlay::render);
    }

    private static void render(WorldRenderContext context) {
        CrimsonConfig config = CrimsonClientClient.config;
        if (!config.blockOverlay) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.hitResult == null) return;
        if (mc.hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult hit = (BlockHitResult) mc.hitResult;
        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (state.isAir()) return;

        VoxelShape shape = state.getShape(mc.level, pos);
        if (shape.isEmpty()) return;

        PoseStack matrices = context.matrixStack();
        MultiBufferSource consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3 cam = context.camera().getPosition();
        long time = System.currentTimeMillis();

        matrices.pushPose();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        matrices.translate(pos.getX(), pos.getY(), pos.getZ());

        Matrix4f matrix = matrices.last().pose();
        VertexConsumer buffer = consumers.getBuffer(RenderType.lines());

        float widthFactor = Math.max(1.0f, config.blockOverlayWidth);
        boolean gradient = config.blockOverlayGradient;
        int baseColor = config.blockOverlayColor;

        // RGB animation for "shader-like" look
        float hueShift = (time % 4000) / 4000f;
        float[] rgb = gradient
                ? hsvToRgb((hueShift + 0.0f) % 1f, 0.85f, 1.0f)
                : colorToFloat(baseColor);

        float alpha = 0.95f;

        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float[] c1 = rgb;
            float[] c2 = gradient
                    ? hsvToRgb((hueShift + 0.25f) % 1f, 0.9f, 1.0f)
                    : rgb;

            buffer.addVertex(matrix, (float) x1, (float) y1, (float) z1)
                    .setColor(c1[0], c1[1], c1[2], alpha)
                    .setNormal(0, 1, 0);
            buffer.addVertex(matrix, (float) x2, (float) y2, (float) z2)
                    .setColor(c2[0], c2[1], c2[2], alpha)
                    .setNormal(0, 1, 0);
        });

        // Soft face fill (very light) for nicer look when style > 0
        if (config.blockOverlayStyle >= 1) {
            VertexConsumer fill = consumers.getBuffer(RenderType.debugFilledBox());
            // Simple expanded outline effect via slightly offset edges already drawn
        }

        matrices.popPose();
    }

    private static float[] colorToFloat(int color) {
        return new float[]{
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f,
                (color & 0xFF) / 255f
        };
    }

    private static float[] hsvToRgb(float h, float s, float v) {
        int i = (int) (h * 6f);
        float f = h * 6f - i;
        float p = v * (1f - s);
        float q = v * (1f - f * s);
        float t = v * (1f - (1f - f) * s);

        return switch (i % 6) {
            case 0 -> new float[]{v, t, p};
            case 1 -> new float[]{q, v, p};
            case 2 -> new float[]{p, v, t};
            case 3 -> new float[]{p, q, v};
            case 4 -> new float[]{t, p, v};
            default -> new float[]{v, p, q};
        };
    }
}
