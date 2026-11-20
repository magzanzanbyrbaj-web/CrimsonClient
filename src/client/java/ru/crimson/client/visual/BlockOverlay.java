
package ru.crimson.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

public final class BlockOverlay {
    private BlockOverlay() {}

    public static void init() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(BlockOverlay::render);
    }

    private static void render(WorldRenderContext context) {
        CrimsonConfig config = CrimsonClientClient.config;
        if (config == null || !config.blockOverlay) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.hitResult == null) return;
        if (mc.hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult hit = (BlockHitResult) mc.hitResult;
        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (state.isAir()) return;

        VoxelShape shape = state.getShape(mc.level, pos);
        if (shape.isEmpty()) return;

        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3 cam = context.gameRenderer().getMainCamera().position();
        float width = Math.max(1.0f, config.blockOverlayWidth);

        matrices.pushPose();
        try {
            matrices.translate(-cam.x, -cam.y, -cam.z);
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());

            Matrix4f matrix = matrices.last().pose();
            VertexConsumer buffer = consumers.getBuffer(RenderTypes.lines());

            boolean gradient = config.blockOverlayGradient;
            float hue = (System.currentTimeMillis() % 4000L) / 4000f;

            float[] first = gradient
                    ? hsvToRgb(hue, 0.85f, 1f)
                    : colorToFloat(config.blockOverlayColor);

            float[] second = gradient
                    ? hsvToRgb((hue + 0.25f) % 1f, 0.9f, 1f)
                    : first;

            shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
                buffer.addVertex(matrix, (float) x1, (float) y1, (float) z1)
                        .setColor(first[0], first[1], first[2], 0.95f)
                        .setNormal(0, 1, 0);
                buffer.setLineWidth(width);

                buffer.addVertex(matrix, (float) x2, (float) y2, (float) z2)
                        .setColor(second[0], second[1], second[2], 0.95f)
                        .setNormal(0, 1, 0);
                buffer.setLineWidth(width);
            });
        } finally {
            matrices.popPose();
        }
    }

    private static float[] colorToFloat(int color) {
        return new float[]{
                ((color >> 16) & 255) / 255f,
                ((color >> 8) & 255) / 255f,
                (color & 255) / 255f
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
