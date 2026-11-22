package ru.crimson.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HudRenderer {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final Identifier FONT =
            Identifier.fromNamespaceAndPath("crimsonclient", "inter");

    private static final Identifier LOGO =
            Identifier.fromNamespaceAndPath("crimsonclient", "crimson_logo.png");

    private static long lastHitmarker;
    private static LivingEntity lastTarget;
    private static long lastTargetTime;

    private static final long TARGET_HUD_DELAY = 3000L;

    private HudRenderer() {}

    public static void init() {
        HudRenderCallback.EVENT.register((g, tickCounter) -> {
            if (mc.player == null || mc.options.hideGui) return;

            CrimsonConfig c = CrimsonClientClient.config;
            if (c == null) return;

            int sw = mc.getWindow().getGuiScaledWidth();
            int sh = mc.getWindow().getGuiScaledHeight();

            if (c.watermark) renderWatermark(g, c, sw);
            if (c.targetHud) renderTarget(g, c, sw, sh);
            if (c.armorHud) renderArmor(g, c, sw, sh);
            if (c.totemCounter) renderTotems(g, c, sw, sh);
            if (c.keystrokes) renderKeys(g, c);
            if (c.moduleList) renderModules(g, c);
            if (c.inventoryHud) renderInventory(g, c, sw, sh);
            if (c.potionHud) renderPotions(g, c, sw);
            if (c.durabilityHud) renderDurability(g, c, sw, sh);

            if (c.customCrosshair) renderCrosshair(g, sw, sh);

            if (c.hitmarker &&
                    System.currentTimeMillis() - lastHitmarker < 200) {
                renderHitmarker(g, sw, sh);
            }

            if (mc.screen instanceof ChatScreen) {
                drawEditorHint(g, sw);
            }
        });
    }

    public static void setTarget(LivingEntity target) {
        if (target == null) return;

        lastTarget = target;
        lastTargetTime = System.currentTimeMillis();
    }

    public static void onHit() {
        lastHitmarker = System.currentTimeMillis();
    }

    private static void text(
            GuiGraphics g,
            String value,
            int x,
            int y,
            int color,
            boolean shadow
    ) {
        Component component = Component.literal(value)
                .setStyle(Style.EMPTY.withFont(
                        new FontDescription.Resource(FONT)
                ));

        g.drawString(mc.font, component, x, y, color, shadow);
    }

    private static int pos(int saved, int fallback) {
        return saved < 0 ? fallback : saved;
    }

    private static void renderWatermark(
            GuiGraphics g,
            CrimsonConfig c,
            int sw
    ) {
        int ping = 0;

        if (mc.getConnection() != null) {
            var info = mc.getConnection()
                    .getPlayerInfo(mc.player.getUUID());

            if (info != null) ping = info.getLatency();
        }

        int w = 154;
        int h = 29;

        int x = pos(c.watermarkX, (sw - w) / 2);
        int y = pos(c.watermarkY, 5);

        panel(g, x, y, w, h);

        g.blit(
                RenderPipelines.GUI_TEXTURED,
                LOGO,
                x + 5, y + 3,
                0f, 0f,
                23, 23,
                128, 128
        );

        text(g, "CRIMSON", x + 34, y + 5,
                0xFFFFF2F4, true);

        text(g, mc.getFps() + " FPS  " + ping + " MS",
                x + 34, y + 17, 0xFFB8B5C2, false);
    }

    private static void renderTarget(
            GuiGraphics g,
            CrimsonConfig c,
            int sw,
            int sh
    ) {
        if (mc.crosshairPickEntity instanceof LivingEntity target
                && target.isAlive()) {
            setTarget(target);
        }

        if (lastTarget == null) return;

        if (System.currentTimeMillis() - lastTargetTime
                > TARGET_HUD_DELAY) {
            lastTarget = null;
            return;
        }

        LivingEntity target = lastTarget;

        int x = pos(c.targetHudX, sw / 2 + 20);
        int y = pos(c.targetHudY, sh / 2 + 25);

        panel(g, x, y, 166, 53);

        String name = target.getName().getString();

        if (name.length() > 15) {
            name = name.substring(0, 12) + "...";
        }

        float hp = Math.max(0, target.getHealth());
        float maxHp = Math.max(1, target.getMaxHealth());
        float percent = Math.min(1f, hp / maxHp);

        text(g, name, x + 10, y + 9,
                0xFFFFFFFF, true);

        text(g, String.format(Locale.ROOT, "%.1f HP", hp),
                x + 10, y + 23, 0xFFFF7584, false);

        String distance = String.format(
                Locale.ROOT,
                "%.1fm",
                mc.player.distanceTo(target)
        );

        text(g, distance, x + 120, y + 23,
                0xFFAAA8B4, false);

        rounded(g, x + 10, y + 39,
                146, 5, 2, 0xFF35212A);

        int fill = (int) (146 * percent);

        if (fill > 0) {
            rounded(g, x + 10, y + 39,
                    fill, 5, 2, 0xFFFF4358);
        }
    }

    private static void renderArmor(
            GuiGraphics g,
            CrimsonConfig c,
            int sw,
            int sh
    ) {
        int x = pos(c.armorHudX, sw - 27);
        int y = pos(c.armorHudY, sh / 2 - 40);

        ItemStack[] armor = {
                mc.player.getItemBySlot(EquipmentSlot.HEAD),
                mc.player.getItemBySlot(EquipmentSlot.CHEST),
                mc.player.getItemBySlot(EquipmentSlot.LEGS),
                mc.player.getItemBySlot(EquipmentSlot.FEET)
        };

        for (int i = 0; i < armor.length; i++) {
            int iy = y + i * 21;

            rounded(g, x, iy, 20, 20, 5, 0xBB111521);

            if (!armor[i].isEmpty()) {
                g.renderItem(armor[i], x + 2, iy + 2);
                g.renderItemDecorations(
                        mc.font, armor[i], x + 2, iy + 2
                );
            }
        }
    }

    private static void renderTotems(
            GuiGraphics g,
            CrimsonConfig c,
            int sw,
            int sh
    ) {
        int count = 0;

        for (int i = 0;
             i < mc.player.getInventory().getContainerSize();
             i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);

            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                count += stack.getCount();
            }
        }

        int x = pos(c.totemHudX, sw / 2 + 90);
        int y = pos(c.totemHudY, sh - 52);

        panel(g, x, y, 45, 23);

        g.renderItem(
                new ItemStack(Items.TOTEM_OF_UNDYING),
                x + 3, y + 3
        );

        text(g, "x" + count, x + 24, y + 8,
                0xFFFFD66B, true);
    }

    private static void renderKeys(
            GuiGraphics g,
            CrimsonConfig c
    ) {
        int x = c.keystrokesX;
        int y = c.keystrokesY;

        int s = 18;
        int gap = 2;

        key(g, x + s + gap, y, s, "W",
                mc.options.keyUp.isDown());

        key(g, x, y + s + gap, s, "A",
                mc.options.keyLeft.isDown());

        key(g, x + s + gap, y + s + gap, s, "S",
                mc.options.keyDown.isDown());

        key(g, x + 2 * (s + gap), y + s + gap, s, "D",
                mc.options.keyRight.isDown());

        key(g, x, y + 2 * (s + gap), 28, "LMB",
                mc.options.keyAttack.isDown());

        key(g, x + 30, y + 2 * (s + gap), 28, "RMB",
                mc.options.keyUse.isDown());
    }

    private static void key(
            GuiGraphics g,
            int x,
            int y,
            int w,
            String label,
            boolean down
    ) {
        rounded(g, x, y, w, 17, 4,
                down ? 0xDD8D293B : 0xBB111521);

        text(g, label,
                x + (w - mc.font.width(label)) / 2,
                y + 5,
                down ? 0xFFFFFFFF : 0xFFD0CCD5,
                false);
    }

    private static void renderModules(
            GuiGraphics g,
            CrimsonConfig c
    ) {
        List<String> modules = new ArrayList<>();

        if (c.watermark) modules.add("Watermark");
        if (c.targetHud) modules.add("Target HUD");
        if (c.armorHud) modules.add("Armor HUD");
        if (c.inventoryHud) modules.add("Inventory HUD");
        if (c.potionHud) modules.add("Potion HUD");
        if (c.keystrokes) modules.add("Keystrokes");
        if (c.totemCounter) modules.add("Totem Counter");
        if (c.targetEsp) modules.add("Target ESP");
        if (c.hitSound) modules.add("Hit Sound");
        if (c.customCrosshair) modules.add("Crosshair");
        if (c.jumpCircle) modules.add("Jump Circle");
        if (c.customHitbox) modules.add("Hitbox");
        if (c.blockOverlay) modules.add("Block Overlay");
        if (c.customSky) modules.add("Custom Sky");
        if (c.noRender) modules.add("No Render");
        if (c.fullbright) modules.add("Fullbright");

        int x = c.moduleListX;
        int y = c.moduleListY;

        for (String name : modules) {
            int w = mc.font.width(name) + 13;

            panel(g, x, y, w, 15);

            text(g, name, x + 6, y + 4,
                    0xFFFF7787, false);

            y += 17;
        }
    }

    private static void renderInventory(
            GuiGraphics g,
            CrimsonConfig c,
            int sw,
            int sh
    ) {
        int slot = 20;
        int gap = 2;

        int width = 9 * slot + 8 * gap;
        int height = 3 * slot + 2 * gap;

        int x = pos(c.inventoryHudX, (sw - width) / 2);
        int y = pos(c.inventoryHudY, sh - height - 12);

        panel(g, x - 3, y - 3, width + 6, height + 6);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = 9 + row * 9 + col;

                int sx = x + col * (slot + gap);
                int sy = y + row * (slot + gap);

                rounded(g, sx, sy, slot, slot, 4, 0xBB111521);

                ItemStack stack =
                        mc.player.getInventory().getItem(index);

                if (!stack.isEmpty()) {
                    g.renderItem(stack, sx + 2, sy + 2);
                    g.renderItemDecorations(
                            mc.font, stack, sx + 2, sy + 2
                    );
                }
            }
        }
    }

    private static void renderPotions(
            GuiGraphics g,
            CrimsonConfig c,
            int sw
    ) {
        int x = pos(c.potionHudX, sw - 130);
        int y = c.potionHudY;

        for (var effect : mc.player.getActiveEffects()) {
            String name = effect.getEffect().value()
                    .getDisplayName().getString();

            int seconds = effect.getDuration() / 20;

            String line = name + " "
                    + (effect.getAmplifier() + 1)
                    + " "
                    + (seconds / 60)
                    + ":"
                    + String.format(Locale.ROOT, "%02d", seconds % 60);

            panel(g, x, y, 125, 16);

            text(g, line, x + 5, y + 4,
                    0xFFB7F5C4, false);

            y += 18;
        }
    }

    private static void renderDurability(
            GuiGraphics g,
            CrimsonConfig c,
            int sw,
            int sh
    ) {
        int x = pos(c.durabilityHudX, sw / 2 + 10);
        int y = pos(c.durabilityHudY, sh - 60);

        ItemStack main = mc.player.getMainHandItem();
        ItemStack off = mc.player.getOffhandItem();

        if (!main.isEmpty() && main.isDamageableItem()) {
            int left = main.getMaxDamage() - main.getDamageValue();

            text(g, left + "/" + main.getMaxDamage(),
                    x, y,
                    left < main.getMaxDamage() / 5
                            ? 0xFFFF4355 : 0xFFCCCCCC,
                    true);
        }

        if (!off.isEmpty() && off.isDamageableItem()) {
            int left = off.getMaxDamage() - off.getDamageValue();

            text(g, left + "/" + off.getMaxDamage(),
                    x, y + 12,
                    left < off.getMaxDamage() / 5
                            ? 0xFFFF4355 : 0xFFCCCCCC,
                    true);
        }
    }

    private static void renderCrosshair(
            GuiGraphics g,
            int sw,
            int sh
    ) {
        CrimsonConfig c = CrimsonClientClient.config;

        int cx = sw / 2;
        int cy = sh / 2;

        int color = c.crosshairColor;
        int size = Math.max(2, Math.round(c.crosshairSize));
        int gap = Math.max(0, Math.round(c.crosshairGap));

        switch (c.crosshairStyle) {
            case 1 -> {
                g.fill(cx - size - gap, cy - 1,
                        cx - gap, cy + 1, color);

                g.fill(cx + gap, cy - 1,
                        cx + size + gap, cy + 1, color);

                g.fill(cx - 1, cy - size - gap,
                        cx + 1, cy - gap, color);

                g.fill(cx - 1, cy + gap,
                        cx + 1, cy + size + gap, color);
            }

            case 2 -> {
                g.fill(cx - size - gap, cy - 1,
                        cx + size + gap + 1, cy + 1, color);

                g.fill(cx - 1, cy + 1,
                        cx + 1, cy + size + gap + 1, color);
            }

            default -> {
                g.fill(cx - size - gap, cy - 1,
                        cx - gap, cy + 1, color);

                g.fill(cx + gap, cy - 1,
                        cx + size + gap + 1, cy + 1, color);

                g.fill(cx - 1, cy - size - gap,
                        cx + 1, cy - gap, color);

                g.fill(cx - 1, cy + gap,
                        cx + 1, cy + size + gap + 1, color);
            }
        }
    }

    private static void renderHitmarker(
            GuiGraphics g,
            int sw,
            int sh
    ) {
        int cx = sw / 2;
        int cy = sh / 2;

        int size = 8;
        int color = 0xAAFF4355;

        g.fill(cx - size, cy - size,
                cx - size + 2, cy - size + 2, color);

        g.fill(cx + size - 2, cy - size,
                cx + size, cy - size + 2, color);

        g.fill(cx - size, cy + size - 2,
                cx - size + 2, cy + size, color);

        g.fill(cx + size - 2, cy + size - 2,
                cx + size, cy + size, color);
    }

    private static void drawEditorHint(
            GuiGraphics g,
            int sw
    ) {
        String hint = "CRIMSON HUD EDITOR";
        int w = mc.font.width(hint) + 18;
        int x = (sw - w) / 2;

        panel(g, x, 38, w, 20);

        text(g, hint, x + 9, 44,
                0xFFFF7585, true);
    }

    private static void panel(
            GuiGraphics g,
            int x,
            int y,
            int w,
            int h
    ) {
        rounded(g, x + 2, y + 2, w, h, 7, 0x44000000);
        rounded(g, x, y, w, h, 7, 0xD910131E);
        rounded(g, x + 1, y + 1, w - 2, 1, 1, 0x88FF5266);
    }

    private static void rounded(
            GuiGraphics g,
            int x,
            int y,
            int w,
            int h,
            int radius,
            int color
    ) {
        if (w <= 0 || h <= 0) return;

        radius = Math.max(
                1,
                Math.min(radius, Math.min(w / 2, h / 2))
        );

        for (int row = 0; row < radius; row++) {
            int inset = (int) Math.ceil(
                    radius - Math.sqrt(
                            radius * radius -
                                    (radius - row) * (radius - row)
                    )
            );

            g.fill(
                    x + inset,
                    y + row,
                    x + w - inset,
                    y + row + 1,
                    color
            );

            g.fill(
                    x + inset,
                    y + h - row - 1,
                    x + w - inset,
                    y + h - row,
                    color
            );
        }

        g.fill(
                x,
                y + radius,
                x + w,
                y + h - radius,
                color
        );
    }
            }
