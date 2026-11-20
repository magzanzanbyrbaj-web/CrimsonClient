
package ru.crimson.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.ArrayList;
import java.util.List;

public class HudRenderer {
    private static final Minecraft mc = Minecraft.getInstance();

    private static final Identifier LOGO =
            Identifier.fromNamespaceAndPath(
                    "crimsonclient",
                    "textures/gui/crimson_logo.png"
            );

    private static long lastHitmarker;

    public static void init() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (mc.player == null || mc.options.hideGui) return;

            CrimsonConfig cfg = CrimsonClientClient.config;

            if (cfg.watermark) renderWatermark(context);
            if (cfg.targetHud) renderTargetHud(context);
            if (cfg.armorHud) renderArmorHud(context);
            if (cfg.totemCounter) renderTotemCounter(context);
            if (cfg.keystrokes) renderKeystrokes(context);
            if (cfg.moduleList) renderModuleList(context);
            if (cfg.potionHud) renderPotionHud(context);
            if (cfg.durabilityHud) renderDurabilityHud(context);
            if (cfg.inventoryHud) renderInventoryHud(context);
            if (cfg.customCrosshair) renderCustomCrosshair(context);

            if (cfg.hitmarker && System.currentTimeMillis() - lastHitmarker < 200) {
                renderHitmarker(context);
            }
        });
    }

    public static void onHit() {
        lastHitmarker = System.currentTimeMillis();
    }

    // WATERMARK

    private static void renderWatermark(GuiGraphics g) {
        int fps = mc.getFps();
        int ping = 0;

        if (mc.getConnection() != null && mc.player != null) {
            var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (info != null) ping = info.getLatency();
        }

        String brand = "CRIMSON";
        String stats = fps + " FPS   " + ping + " MS";

        int logoSize = 25;
        int gap = 8;
        int padding = 12;

        int brandWidth = mc.font.width(brand);
        int statsWidth = mc.font.width(stats);

        int width = padding + logoSize + gap + brandWidth
                + 13 + statsWidth + padding;

        int height = 34;
        int x = (mc.getWindow().getGuiScaledWidth() - width) / 2;
        int y = 7;

        rounded(g, x + 2, y + 3, width, height, 9, 0x44000000);
        rounded(g, x, y, width, height, 9, 0xD90D101A);

        rounded(g, x + 1, y + 1, width - 2, 1, 1, 0x99FF4355);
        rounded(g, x + 1, y + height - 2, width - 2, 1, 1, 0x445E6577);

        g.blit(
                LOGO,
                x + padding,
                y + 4,
                0, 0,
                logoSize, logoSize,
                256, 256
        );

        int textX = x + padding + logoSize + gap;

        g.drawString(
                mc.font,
                brand,
                textX,
                y + 7,
                0xFFFFF2F3,
                true
        );

        g.drawString(
                mc.font,
                "CLIENT",
                textX,
                y + 19,
                0xFFAAA7B4,
                false
        );

        int dividerX = textX + brandWidth + 10;

        g.fill(
                dividerX,
                y + 8,
                dividerX + 1,
                y + 26,
                0x88FF4355
        );

        g.drawString(
                mc.font,
                stats,
                dividerX + 9,
                y + 13,
                0xFFE4E1E8,
                false
        );
    }

    // TARGET HUD

    private static void renderTargetHud(GuiGraphics g) {
        Entity target = mc.crosshairPickEntity;

        if (!(target instanceof LivingEntity living)) return;
        if (!living.isAlive()) return;

        int screenW = mc.getWindow().getGuiScaledWidth();

        int width = 178;
        int height = 57;

        int x = screenW / 2 + 20;
        int y = mc.getWindow().getGuiScaledHeight() / 2 + 25;

        if (x + width > screenW - 5) {
            x = screenW - width - 5;
        }

        rounded(g, x + 2, y + 3, width, height, 10, 0x55000000);
        rounded(g, x, y, width, height, 10, 0xD90D101A);

        rounded(g, x + 1, y + 1, width - 2, 1, 1, 0xAAFF4355);
        rounded(g, x + 1, y + height - 2, width - 2, 1, 1, 0x445E6577);

        rounded(g, x + 8, y + 9, 35, 35, 8, 0xAA3A1925);

        g.drawString(
                mc.font,
                "C",
                x + 21,
                y + 22,
                0xFFFF4355,
                true
        );

        String name = living.getName().getString();

        if (name.length() > 17) {
            name = name.substring(0, 14) + "...";
        }

        float health = Math.max(0, living.getHealth());
        float maxHealth = Math.max(1, living.getMaxHealth());
        float healthPercent = Math.min(1f, health / maxHealth);

        float distance = mc.player.distanceTo(living);

        g.drawString(
                mc.font,
                name,
                x + 51,
                y + 10,
                0xFFFFF2F3,
                true
        );

        String hpText = String.format("%.1f HP", health);
        String distanceText = String.format("%.1fm", distance);

        g.drawString(
                mc.font,
                hpText,
                x + 51,
                y + 23,
                0xFFFF737F,
                false
        );

        g.drawString(
                mc.font,
                distanceText,
                x + width - mc.font.width(distanceText) - 10,
                y + 23,
                0xFFAAA7B4,
                false
        );

        int barX = x + 51;
        int barY = y + 39;
        int barW = width - 62;

        rounded(g, barX, barY, barW, 6, 3, 0xFF33202A);

        int filled = (int) (barW * healthPercent);

        if (filled > 0) {
            rounded(g, barX, barY, filled, 6, 3, 0xFFFF4355);
        }
    }

    // ARMOR HUD

    private static void renderArmorHud(GuiGraphics g) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        int x = mc.getWindow().getGuiScaledWidth() - 25;
        int y = mc.getWindow().getGuiScaledHeight() / 2 - 40;

        ItemStack[] armor = {
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET)
        };

        for (int i = 0; i < armor.length; i++) {
            ItemStack stack = armor[i];
            int iy = y + i * 20;

            rounded(g, x - 3, iy - 3, 22, 22, 5, 0xAA11141E);

            if (!stack.isEmpty()) {
                g.renderItem(stack, x, iy);
                g.renderItemDecorations(mc.font, stack, x, iy);
            }
        }
    }

    // TOTEM COUNTER

    private static void renderTotemCounter(GuiGraphics g) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        int count = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                count += stack.getCount();
            }
        }

        if (count == 0) return;

        int x = mc.getWindow().getGuiScaledWidth() / 2 + 90;
        int y = mc.getWindow().getGuiScaledHeight() - 50;

        rounded(g, x, y, 43, 21, 6, 0xCC11141E);

        g.renderItem(new ItemStack(Items.TOTEM_OF_UNDYING), x + 2, y + 2);

        g.drawString(
                mc.font,
                "x" + count,
                x + 22,
                y + 7,
                0xFFFFD66B,
                true
        );
    }

    // KEYSTROKES

    private static void renderKeystrokes(GuiGraphics g) {
        int x = 5;
        int y = 43;
        int size = 18;
        int gap = 2;

        drawKey(g, x + size + gap, y, size, "W", mc.options.keyUp.isDown());
        drawKey(g, x, y + size + gap, size, "A", mc.options.keyLeft.isDown());
        drawKey(g, x + size + gap, y + size + gap, size, "S", mc.options.keyDown.isDown());
        drawKey(g, x + 2 * (size + gap), y + size + gap,
                size, "D", mc.options.keyRight.isDown());

        drawKey(g, x, y + 2 * (size + gap),
                size + (size + gap) / 2, "LMB", mc.options.keyAttack.isDown());

        drawKey(g, x + size + gap + (size + gap) / 2,
                y + 2 * (size + gap),
                size + (size + gap) / 2, "RMB", mc.options.keyUse.isDown());
    }

    private static void drawKey(GuiGraphics g, int x, int y, int w,
                                String label, boolean pressed) {
        rounded(g, x, y, w, 16, 4,
                pressed ? 0xCCAA2638 : 0xAA11141E);

        rounded(g, x, y, w, 1, 1,
                pressed ? 0xFFFF5365 : 0xFF4A2933);

        int textW = mc.font.width(label);

        g.drawString(
                mc.font,
                label,
                x + (w - textW) / 2,
                y + 4,
                pressed ? 0xFFFFFFFF : 0xFFCCCCD4,
                false
        );
    }

    // MODULE LIST

    private static void renderModuleList(GuiGraphics g) {
        CrimsonConfig cfg = CrimsonClientClient.config;
        List<String> modules = new ArrayList<>();

        if (cfg.watermark) modules.add("Watermark");
        if (cfg.targetHud) modules.add("Target HUD");
        if (cfg.armorHud) modules.add("Armor HUD");
        if (cfg.totemCounter) modules.add("Totem Counter");
        if (cfg.keystrokes) modules.add("Keystrokes");
        if (cfg.inventoryHud) modules.add("Inventory HUD");
        if (cfg.potionHud) modules.add("Potion HUD");
        if (cfg.durabilityHud) modules.add("Durability HUD");
        if (cfg.hitParticles) modules.add("Hit Particles");
        if (cfg.damageNumbers) modules.add("Damage Numbers");
        if (cfg.hitmarker) modules.add("Hitmarker");
        if (cfg.customCrosshair) modules.add("Crosshair");
        if (cfg.fullbright) modules.add("Fullbright");

        int x = 4;
        int y = 100;

        for (String module : modules) {
            int textW = mc.font.width(module);

            rounded(g, x, y, textW + 12, 15, 4, 0xAA11141E);

            g.drawString(
                    mc.font,
                    module,
                    x + 6,
                    y + 4,
                    0xFFFF7885,
                    false
            );

            y += 17;
        }
    }

    // POTION HUD

    private static void renderPotionHud(GuiGraphics g) {
        LocalPlayer player = mc.player;

        if (player == null || player.getActiveEffects().isEmpty()) return;

        int x = mc.getWindow().getGuiScaledWidth() - 130;
        int y = 42;

        for (var effect : player.getActiveEffects()) {
            String name = effect.getEffect().value().getDisplayName().getString();

            int amplifier = effect.getAmplifier() + 1;
            int seconds = effect.getDuration() / 20;

            String text = name + " " + amplifier + " "
                    + (seconds / 60) + ":"
                    + String.format("%02d", seconds % 60);

            rounded(g, x, y, 125, 16, 5, 0xAA11141E);

            g.drawString(
                    mc.font,
                    text,
                    x + 6,
                    y + 4,
                    0xFFB7F5C4,
                    false
            );

            y += 18;
        }
    }

    // DURABILITY HUD

    private static void renderDurabilityHud(GuiGraphics g) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        int x = mc.getWindow().getGuiScaledWidth() / 2 + 10;
        int y = mc.getWindow().getGuiScaledHeight() - 60;

        if (!main.isEmpty() && main.isDamageableItem()) {
            int max = main.getMaxDamage();
            int left = max - main.getDamageValue();

            g.drawString(
                    mc.font,
                    left + "/" + max,
                    x,
                    y,
                    left < max / 5 ? 0xFFFF4355 : 0xFFAAAAAA,
                    true
            );
        }

        if (!off.isEmpty() && off.isDamageableItem()) {
            int max = off.getMaxDamage();
            int left = max - off.getDamageValue();

            g.drawString(
                    mc.font,
                    left + "/" + max,
                    x,
                    y + 12,
                    left < max / 5 ? 0xFFFF4355 : 0xFFAAAAAA,
                    true
            );
        }
    }

    // INVENTORY HUD

    private static void renderInventoryHud(GuiGraphics g) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        int slot = 20;
        int totalW = slot * 9;

        int x = (mc.getWindow().getGuiScaledWidth() - totalW) / 2;
        int y = mc.getWindow().getGuiScaledHeight() - 91;

        for (int i = 0; i < 9; i++) {
            int sx = x + i * slot;

            rounded(g, sx + 1, y, 18, 18, 4, 0xB511141E);
            rounded(g, sx + 1, y, 18, 1, 1, 0x99FF5365);

            ItemStack stack = player.getInventory().getItem(i + 9);

            if (!stack.isEmpty()) {
                g.renderItem(stack, sx + 2, y + 1);
                g.renderItemDecorations(mc.font, stack, sx + 2, y + 1);
            }
        }
    }

    // CUSTOM CROSSHAIR

    private static void renderCustomCrosshair(GuiGraphics g) {
        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;

        g.fill(cx - 6, cy - 1, cx + 7, cy + 1, 0xFFFF4355);
        g.fill(cx - 1, cy - 6, cx + 1, cy + 7, 0xFFFF4355);
        g.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFFFFFFFF);
    }

    // HITMARKER

    private static void renderHitmarker(GuiGraphics g) {
        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;

        int size = 8;
        int color = 0xAAFF4355;

        g.fill(cx - size, cy - size, cx - size + 2, cy - size + 2, color);
        g.fill(cx + size - 2, cy - size, cx + size, cy - size + 2, color);
        g.fill(cx - size, cy + size - 2, cx - size + 2, cy + size, color);
        g.fill(cx + size - 2, cy + size - 2, cx + size, cy + size, color);
    }

    // ROUNDED RECTANGLE

    private static void rounded(GuiGraphics g, int x, int y, int w,
                                int h, int radius, int color) {
        if (w <= 0 || h <= 0) return;

        int r = Math.max(1, Math.min(radius, Math.min(w / 2, h / 2)));

        for (int row = 0; row < r; row++) {
            int inset = (int) Math.ceil(r - Math.sqrt(
                    r * r - (r - row) * (r - row)));

            g.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
            g.fill(x + inset, y + h - row - 1,
                    x + w - inset, y + h - row, color);
        }

        g.fill(x, y + r, x + w, y + h - r, color);
    }
}
