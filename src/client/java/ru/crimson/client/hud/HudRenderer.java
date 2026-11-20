
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
            Identifier.fromNamespaceAndPath("crimsonclient", "textures/gui/crimson_logo.png");

    private static long lastHitmarker = 0;

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

    private static void renderWatermark(GuiGraphics g) {
        int fps = mc.getFps();
        int ping = 0;

        if (mc.getConnection() != null && mc.player != null) {
            var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (info != null) ping = info.getLatency();
        }

        String stats = fps + " FPS  •  " + ping + " MS";
        String brand = "CRIMSON";

        int logoSize = 24;
        int h = 30;

        int w = 12 + logoSize + 7 + mc.font.width(brand)
                + 12 + mc.font.width(stats) + 22;

        int x = (mc.getWindow().getGuiScaledWidth() - w) / 2;
        int y = 6;

        roundedRect(g, x, y, w, h, 8, 0xC9141012);
        roundedRect(g, x + 1, y + 1, w - 2, 1, 1, 0xCCFF4444);

        g.blit(LOGO, x + 7, y + 3, 0, 0, logoSize, logoSize, 256, 256);

        int textX = x + 7 + logoSize + 6;

        g.drawString(mc.font, brand, textX, y + 6, 0xFFFFEEEE, true);

        int dividerX = textX + mc.font.width(brand) + 7;

        g.fill(dividerX, y + 7, dividerX + 1, y + 23, 0x88FF5555);

        g.drawString(mc.font, stats, dividerX + 7, y + 11, 0xFFD9CACA, false);
    }

    private static void renderTargetHud(GuiGraphics g) {
        Entity target = mc.crosshairPickEntity;

        if (!(target instanceof LivingEntity living) || !living.isAlive()) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int x = screenW / 2 - 60;
        int y = 40;
        int w = 120;
        int h = 36;

        roundedRect(g, x, y, w, h, 6, 0xCC1A0808);
        roundedRect(g, x, y, w, 1, 1, 0xFFFF3333);

        String name = living.getName().getString();
        float hp = living.getHealth();
        float maxHp = living.getMaxHealth();
        float dist = mc.player.distanceTo(living);

        g.drawString(mc.font, name, x + 7, y + 5, 0xFFFFEEEE, false);

        String hpStr = String.format("%.1f / %.1f", hp, maxHp);
        g.drawString(mc.font, hpStr, x + 7, y + 15, 0xFFFF7777, false);

        String distance = String.format("%.1fm", dist);
        g.drawString(mc.font, distance,
                x + w - mc.font.width(distance) - 7, y + 5, 0xFFAAAAAA, false);

        int barW = w - 14;
        int barX = x + 7;
        int barY = y + 27;

        roundedRect(g, barX, barY, barW, 4, 2, 0xFF330000);

        int filled = (int) (barW * (hp / Math.max(maxHp, 0.1f)));

        if (filled > 0) {
            roundedRect(g, barX, barY, filled, 4, 2, 0xFFFF3333);
        }
    }

    private static void renderArmorHud(GuiGraphics g) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        int x = mc.getWindow().getGuiScaledWidth() - 24;
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

            roundedRect(g, x - 3, iy - 3, 22, 22, 5, 0xAA1A1012);

            if (!stack.isEmpty()) {
                g.renderItem(stack, x, iy);
                g.renderItemDecorations(mc.font, stack, x, iy);
            }
        }
    }

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

        roundedRect(g, x, y, 42, 20, 5, 0xAA1A1012);

        g.renderItem(new ItemStack(Items.TOTEM_OF_UNDYING), x + 2, y + 2);
        g.drawString(mc.font, "x" + count, x + 22, y + 6, 0xFFFFFF55, true);
    }

    private static void renderKeystrokes(GuiGraphics g) {
        int x = 4;
        int y = 42;
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
                y + 2 * (size + gap), size + (size + gap) / 2,
                "RMB", mc.options.keyUse.isDown());
    }

    private static void drawKey(GuiGraphics g, int x, int y, int w,
                                String label, boolean pressed) {
        roundedRect(g, x, y, w, 16, 4,
                pressed ? 0xCCAA2222 : 0xAA1A1012);

        roundedRect(g, x, y, w, 1, 1,
                pressed ? 0xFFFF5555 : 0xFF662222);

        int tw = mc.font.width(label);

        g.drawString(mc.font, label, x + (w - tw) / 2, y + 4,
                pressed ? 0xFFFFFFFF : 0xFFCCCCCC, false);
    }

    private static void renderModuleList(GuiGraphics g) {
        CrimsonConfig cfg = CrimsonClientClient.config;
        List<String> modules = new ArrayList<>();

        if (cfg.watermark) modules.add("Watermark");
        if (cfg.targetHud) modules.add("TargetHUD");
        if (cfg.armorHud) modules.add("ArmorHUD");
        if (cfg.totemCounter) modules.add("TotemCounter");
        if (cfg.keystrokes) modules.add("Keystrokes");
        if (cfg.moduleList) modules.add("ModuleList");
        if (cfg.inventoryHud) modules.add("InventoryHUD");
        if (cfg.potionHud) modules.add("PotionHUD");
        if (cfg.durabilityHud) modules.add("DurabilityHUD");
        if (cfg.hitParticles) modules.add("HitParticles");
        if (cfg.damageNumbers) modules.add("DamageNumbers");
        if (cfg.hitmarker) modules.add("Hitmarker");
        if (cfg.criticalEffects) modules.add("CritEffects");
        if (cfg.smoothSword) modules.add("SmoothSword");
        if (cfg.itemSwitchAnim) modules.add("ItemSwitch");
        if (cfg.totemAnimation) modules.add("TotemAnim");
        if (cfg.customCrosshair) modules.add("Crosshair");
        if (cfg.fullbright) modules.add("Fullbright");
        if (cfg.customSky) modules.add("CustomSky");
        if (cfg.inventoryBlur) modules.add("InvBlur");

        int x = 4;
        int y = 100;

        for (String module : modules) {
            int tw = mc.font.width(module);

            roundedRect(g, x, y, tw + 10, 13, 4, 0xAA1A1012);
            g.drawString(mc.font, module, x + 5, y + 3, 0xFFFF7777, false);

            y += 15;
        }
    }

    private static void renderPotionHud(GuiGraphics g) {
        LocalPlayer player = mc.player;

        if (player == null || player.getActiveEffects().isEmpty()) return;

        int x = mc.getWindow().getGuiScaledWidth() - 125;
        int y = 42;

        for (var effect : player.getActiveEffects()) {
            String name = effect.getEffect().value().getDisplayName().getString();

            int amplifier = effect.getAmplifier() + 1;
            int seconds = effect.getDuration() / 20;

            String text = name + " " + amplifier + " "
                    + (seconds / 60) + ":" + String.format("%02d", seconds % 60);

            roundedRect(g, x, y, 120, 14, 4, 0xAA1A1012);
            g.drawString(mc.font, text, x + 5, y + 3, 0xFFAAFFAA, false);

            y += 16;
        }
    }

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

            g.drawString(mc.font, left + "/" + max, x, y,
                    left < max / 5 ? 0xFFFF3333 : 0xFFAAAAAA, true);
        }

        if (!off.isEmpty() && off.isDamageableItem()) {
            int max = off.getMaxDamage();
            int left = max - off.getDamageValue();

            g.drawString(mc.font, left + "/" + max, x, y + 12,
                    left < max / 5 ? 0xFFFF3333 : 0xFFAAAAAA, true);
        }
    }

    private static void renderInventoryHud(GuiGraphics g) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        int slot = 20;
        int totalW = slot * 9;

        int x = (mc.getWindow().getGuiScaledWidth() - totalW) / 2;
        int y = mc.getWindow().getGuiScaledHeight() - 66;

        for (int i = 0; i < 9; i++) {
            int sx = x + i * slot;

            roundedRect(g, sx + 1, y, 18, 18, 4, 0xB51A1012);
            roundedRect(g, sx + 1, y, 18, 1, 1, 0x99FF5555);

            ItemStack stack = player.getInventory().getItem(i + 9);

            if (!stack.isEmpty()) {
                g.renderItem(stack, sx + 2, y + 1);
                g.renderItemDecorations(mc.font, stack, sx + 2, y + 1);
            }
        }
    }

    private static void renderCustomCrosshair(GuiGraphics g) {
        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;

        g.fill(cx - 6, cy - 1, cx + 7, cy + 1, 0xFFFF3333);
        g.fill(cx - 1, cy - 6, cx + 1, cy + 7, 0xFFFF3333);
        g.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFFFFFFFF);
    }

    private static void renderHitmarker(GuiGraphics g) {
        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;

        int size = 8;
        int color = 0xAAFF3333;

        g.fill(cx - size, cy - size, cx - size + 2, cy - size + 2, color);
        g.fill(cx + size - 2, cy - size, cx + size, cy - size + 2, color);
        g.fill(cx - size, cy + size - 2, cx - size + 2, cy + size, color);
        g.fill(cx + size - 2, cy + size - 2, cx + size, cy + size, color);
    }

    private static void roundedRect(GuiGraphics g, int x, int y, int w,
                                    int h, int r, int color) {
        if (w <= 0 || h <= 0) return;

        int radius = Math.max(1, Math.min(r, Math.min(w / 2, h / 2)));

        for (int row = 0; row < radius; row++) {
            int inset = (int) Math.ceil(radius - Math.sqrt(
                    radius * radius - (radius - row) * (radius - row)));

            g.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
            g.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, color);
        }

        g.fill(x, y + radius, x + w, y + h - radius, color);
    }
}
