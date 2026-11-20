package ru.crimson.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.ArrayList;
import java.util.List;

public class HudRenderer {

    private static final Minecraft mc = Minecraft.getInstance();
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

            if (cfg.hitmarker &&
                System.currentTimeMillis() - lastHitmarker < 200) {
                renderHitmarker(context);
            }
        });
    }

    public static void onHit() {
        lastHitmarker = System.currentTimeMillis();
    }

    // WATERMARK

    private static void renderWatermark(GuiGraphics g) {
        CrimsonConfig cfg = CrimsonClientClient.config;

        int fps = mc.getFps();
        int ping = 0;

        if (mc.getConnection() != null && mc.player != null) {
            var info = mc.getConnection()
                .getPlayerInfo(mc.player.getUUID());

            if (info != null) ping = info.getLatency();
        }

        String text = cfg.watermarkText
            + " | " + fps + " FPS | " + ping + " ms";

        int x = cfg.watermarkX;
        int y = cfg.watermarkY;
        int w = mc.font.width(text) + 8;

        g.fill(x, y, x + w, y + 12, 0xAA1A0505);
        g.fill(x, y, x + w, y + 1, 0xFFFF3333);

        g.drawString(
            mc.font, text,
            x + 4, y + 2,
            0xFFFFEEEE, true
        );
    }

    // TARGET HUD

    private static void renderTargetHud(GuiGraphics g) {
        Entity target = mc.crosshairPickEntity;

        if (!(target instanceof LivingEntity living)
            || !living.isAlive()) return;

        int screenW = mc.getWindow().getGuiScaledWidth();

        int x = screenW / 2 - 60;
        int y = 40;
        int w = 120;
        int h = 36;

        g.fill(x, y, x + w, y + h, 0xCC1A0808);
        g.fill(x, y, x + w, y + 1, 0xFFFF3333);
        g.fill(x, y + h - 1, x + w, y + h, 0xFFFF3333);

        String name = living.getName().getString();

        float hp = living.getHealth();
        float maxHp = living.getMaxHealth();
        float dist = mc.player.distanceTo(living);

        g.drawString(
            mc.font, name,
            x + 4, y + 4,
            0xFFFFEEEE, false
        );

        String hpStr = String.format("%.1f / %.1f", hp, maxHp);

        g.drawString(
            mc.font, hpStr,
            x + 4, y + 14,
            0xFFFF6666, false
        );

        String distance = String.format("%.1fm", dist);

        g.drawString(
            mc.font, distance,
            x + w - mc.font.width(distance) - 4,
            y + 4,
            0xFFAAAAAA, false
        );

        int barW = w - 8;
        int barX = x + 4;
        int barY = y + 26;

        g.fill(
            barX, barY,
            barX + barW, barY + 4,
            0xFF330000
        );

        int filled = (int) (
            barW * (hp / Math.max(maxHp, 0.1f))
        );

        g.fill(
            barX, barY,
            barX + filled, barY + 4,
            0xFFFF3333
        );
    }

    // ARMOR HUD

    private static void renderArmorHud(GuiGraphics g) {
        LocalPlayer player = mc.player;

        if (player == null) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int x = screenW - 24;
        int y = screenH / 2 - 40;

        ItemStack[] armor = {
            player.getInventory().getArmor(3),
            player.getInventory().getArmor(2),
            player.getInventory().getArmor(1),
            player.getInventory().getArmor(0)
        };

        for (int i = 0; i < 4; i++) {
            ItemStack stack = armor[i];

            int iy = y + i * 20;

            g.fill(
                x - 2, iy - 2,
                x + 18, iy + 18,
                0xAA1A0505
            );

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

        for (int i = 0;
             i < player.getInventory().getContainerSize();
             i++) {

            ItemStack s = player.getInventory().getItem(i);

            if (s.is(Items.TOTEM_OF_UNDYING)) {
                count += s.getCount();
            }
        }

        if (count == 0) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int x = screenW / 2 + 90;
        int y = screenH - 50;

        g.fill(x, y, x + 40, y + 18, 0xAA1A0505);

        g.renderItem(
            new ItemStack(Items.TOTEM_OF_UNDYING),
            x + 2, y + 1
        );

        g.drawString(
            mc.font, "x" + count,
            x + 20, y + 5,
            0xFFFFFF55, true
        );
    }

    // KEYSTROKES

    private static void renderKeystrokes(GuiGraphics g) {
        int x = 4;
        int y = 30;
        int size = 18;
        int gap = 2;

        boolean w = mc.options.keyUp.isDown();
        boolean a = mc.options.keyLeft.isDown();
        boolean s = mc.options.keyDown.isDown();
        boolean d = mc.options.keyRight.isDown();

        boolean lmb = mc.options.keyAttack.isDown();
        boolean rmb = mc.options.keyUse.isDown();

        drawKey(g, x + size + gap, y, size, "W", w);

        drawKey(
            g, x, y + size + gap,
            size, "A", a
        );

        drawKey(
            g, x + size + gap, y + size + gap,
            size, "S", s
        );

        drawKey(
            g, x + 2 * (size + gap),
            y + size + gap,
            size, "D", d
        );

        drawKey(
            g, x, y + 2 * (size + gap),
            size + (size + gap) / 2,
            "LMB", lmb
        );

        drawKey(
            g,
            x + size + gap + (size + gap) / 2,
            y + 2 * (size + gap),
            size + (size + gap) / 2,
            "RMB", rmb
        );
    }

    private static void drawKey(
        GuiGraphics g,
        int x, int y, int w,
        String label,
        boolean pressed
    ) {
        int bg = pressed
            ? 0xCCAA2222
            : 0xAA1A0505;

        int border = pressed
            ? 0xFFFF5555
            : 0xFF662222;

        g.fill(x, y, x + w, y + 16, bg);

        g.fill(x, y, x + w, y + 1, border);
        g.fill(x, y + 15, x + w, y + 16, border);
        g.fill(x, y, x + 1, y + 16, border);
        g.fill(x + w - 1, y, x + w, y + 16, border);

        int tw = mc.font.width(label);

        g.drawString(
            mc.font, label,
            x + (w - tw) / 2,
            y + 4,
            pressed ? 0xFFFFFFFF : 0xFFCCCCCC,
            false
        );
    }

    // MODULE LIST

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

        for (String m : modules) {
            int tw = mc.font.width(m);

            g.fill(
                x, y,
                x + tw + 6, y + 11,
                0xAA1A0505
            );

            g.drawString(
                mc.font, m,
                x + 3, y + 1,
                0xFFFF6666, false
            );

            y += 12;
        }
    }

    // POTION HUD

    private static void renderPotionHud(GuiGraphics g) {
        LocalPlayer player = mc.player;

        if (player == null || player.getActiveEffects().isEmpty()) {
            return;
        }

        int x = mc.getWindow().getGuiScaledWidth() - 120;
        int y = 30;

        for (var effect : player.getActiveEffects()) {

            String name = effect.getEffect()
                .value()
                .getDisplayName()
                .getString();

            int amp = effect.getAmplifier() + 1;
            int sec = effect.getDuration() / 20;

            String text = name + " " + amp + " "
                + (sec / 60) + ":"
                + String.format("%02d", sec % 60);

            g.fill(x, y, x + 115, y + 12, 0xAA1A0505);

            g.drawString(
                mc.font, text,
                x + 3, y + 2,
                0xFFAAFFAA, false
            );

            y += 14;
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
            int dmg = main.getDamageValue();
            int left = max - dmg;

            String t = left + "/" + max;

            g.drawString(
                mc.font, t,
                x, y,
                left < max / 5 ? 0xFFFF3333 : 0xFFAAAAAA,
                true
            );
        }

        if (!off.isEmpty() && off.isDamageableItem()) {

            int max = off.getMaxDamage();
            int dmg = off.getDamageValue();
            int left = max - dmg;

            String t = left + "/" + max;

            g.drawString(
                mc.font, t,
                x, y + 12,
                left < max / 5 ? 0xFFFF3333 : 0xFFAAAAAA,
                true
            );
        }
    }

    // INVENTORY HUD

    private static void renderInventoryHud(GuiGraphics g) {
        g.drawString(
            mc.font,
            "Inv",
            4,
            mc.getWindow().getGuiScaledHeight() - 20,
            0xFF888888,
            false
        );
    }

    // CUSTOM CROSSHAIR

    private static void renderCustomCrosshair(GuiGraphics g) {
        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;

        int size = 6;
        int color = 0xFFFF3333;

        g.fill(
            cx - size, cy - 1,
            cx + size + 1, cy + 1,
            color
        );

        g.fill(
            cx - 1, cy - size,
            cx + 1, cy + size + 1,
            color
        );

        g.fill(
            cx - 1, cy - 1,
            cx + 1, cy + 1,
            0xFFFFFFFF
        );
    }

    // HITMARKER

    private static void renderHitmarker(GuiGraphics g) {
        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;

        int s = 8;
        int color = 0xAAFF3333;

        g.fill(cx - s, cy - s, cx - s + 2, cy - s + 2, color);
        g.fill(cx + s - 2, cy - s, cx + s, cy - s + 2, color);
        g.fill(cx - s, cy + s - 2, cx - s + 2, cy + s, color);
        g.fill(cx + s - 2, cy + s - 2, cx + s, cy + s, color);
    }
          }
