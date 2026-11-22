
package ru.crimson.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

public final class ChestESP {

    private static final List<BlockPos> CHESTS = new ArrayList<>();
    private static int tickCounter;

    private static final int SCAN_RADIUS = 12;

    private ChestESP() {}

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CrimsonConfig cfg = CrimsonClientClient.config;

            if (cfg == null || !cfg.chestEsp
                    || client.player == null
                    || client.level == null) {
                CHESTS.clear();
                return;
            }

            if (++tickCounter >= 10) {
                tickCounter = 0;
                scan(client);
            }
        });

        WorldRenderEvents.BEFORE_TRANSLUCENT.register(
                ChestESP::render
        );
    }

    private static void scan(Minecraft mc) {
        CHESTS.clear();

        if (mc.player == null || mc.level == null) {
            return;
        }

        CrimsonConfig cfg = CrimsonClientClient.config;
        if (cfg == null || !cfg.chestEsp) return;

        BlockPos center = mc.player.blockPosition();
        BlockPos.MutableBlockPos pos =
                new BlockPos.MutableBlockPos();

        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {

                    pos.set(
                            center.getX() + x,
                            center.getY() + y,
                            center.getZ() + z
                    );

                    BlockState state = mc.level.getBlockState(pos);

                    if (isContainer(state, cfg)) {
                        CHESTS.add(pos.immutable());
                    }
                }
            }
        }
    }

    private static boolean isContainer(
            BlockState state,
            CrimsonConfig cfg
    ) {
        Block block = state.getBlock();

        if (block == Blocks.CHEST) {
            return cfg.chestEspNormal;
        }

        if (block == Blocks.TRAPPED_CHEST) {
            return cfg.chestEspTrapped;
        }

        if (block == Blocks.ENDER_CHEST) {
            return cfg.chestEspEnder;
        }

        if (block == Blocks.BARREL) {
            return cfg.chestEspBarrel;
        }

        return block instanceof ShulkerBoxBlock
                && cfg.chestEspShulker;
    }

    private static void render(WorldRenderContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        CrimsonConfig cfg = CrimsonClientClient.config;

        if (cfg == null || !cfg.chestEsp
                || mc.player == null
                || mc.level == null
                || CHESTS.isEmpty()) {
            return;
        }

        PoseStack matrices = ctx.matrices();
        MultiBufferSource consumers = ctx.consumers();

        if (matrices == null || consumers == null) {
            return;
        }

        Vec3 camera = ctx.gameRenderer()
                .getMainCamera().position();

        int color = cfg.chestEspColor;

        float red = ((color >> 16) & 255) / 255f;
        float green = ((color >> 8) & 255) / 255f;
        float blue = (color & 255) / 255f;
        float alpha = ((color >>> 24) & 255) / 255f;

        matrices.pushPose();

        try {
            matrices.translate(
                    -camera.x,
                    -camera.y,
                    -camera.z
            );

            Matrix4f matrix = matrices.last().pose();

            VertexConsumer buffer =
                    consumers.getBuffer(RenderTypes.lines());

            for (BlockPos pos : CHESTS) {
                if (!mc.level.hasChunkAt(pos)) continue;

                AABB box = new AABB(pos);

                drawBox(
                        buffer,
                        matrix,
                        box,
                        red,
                        green,
                        blue,
                        alpha,
                        cfg.chestEspLineWidth
                );
            }
        } finally {
            matrices.popPose();
        }
    }

    private static void drawBox(
            VertexConsumer buffer,
            Matrix4f matrix,
            AABB box,
            float r,
            float g,
            float b,
            float a,
            float width
    ) {
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;

        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        line(buffer, matrix, x1,y1,z1, x2,y1,z1,r,g,b,a,width);
        line(buffer, matrix, x2,y1,z1, x2,y1,z2,r,g,b,a,width);
        line(buffer, matrix, x2,y1,z2, x1,y1,z2,r,g,b,a,width);
        line(buffer, matrix, x1,y1,z2, x1,y1,z1,r,g,b,a,width);

        line(buffer, matrix, x1,y2,z1, x2,y2,z1,r,g,b,a,width);
        line(buffer, matrix, x2,y2,z1, x2,y2,z2,r,g,b,a,width);
        line(buffer, matrix, x2,y2,z2, x1,y2,z2,r,g,b,a,width);
        line(buffer, matrix, x1,y2,z2, x1,y2,z1,r,g,b,a,width);

        line(buffer, matrix, x1,y1,z1, x1,y2,z1,r,g,b,a,width);
        line(buffer, matrix, x2,y1,z1, x2,y2,z1,r,g,b,a,width);
        line(buffer, matrix, x2,y1,z2, x2,y2,z2,r,g,b,a,width);
        line(buffer, matrix, x1,y1,z2, x1,y2,z2,r,g,b,a,width);
    }

    private static void line(
            VertexConsumer buffer,
            Matrix4f matrix,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a,
            float width
    ) {
        buffer.addVertex(matrix, x1, y1, z1)
                .setColor(r, g, b, a)
                .setLineWidth(width)
                .setNormal(0, 1, 0);

        buffer.addVertex(matrix, x2, y2, z2)
                .setColor(r, g, b, a)
                .setLineWidth(width)
                .setNormal(0, 1, 0);
    }
}
