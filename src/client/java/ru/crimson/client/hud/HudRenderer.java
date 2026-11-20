
package ru.crimson.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.ArrayList;
import java.util.List;

public class HudRenderer {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final Identifier FONT =
            Identifier.fromNamespaceAndPath("crimsonclient", "inter");
    private static final Identifier LOGO =
            Identifier.fromNamespaceAndPath("crimsonclient", "crimson_logo.png");

    private static long lastHitmarker;

    private static String dragging;
    private static boolean wasMouseDown;
    private static double dragOffsetX;
    private static double dragOffsetY;

    public static void init() {
        HudRenderCallback.EVENT.register((g, tickCounter) -> {
            if (mc.player == null || mc.options.hideGui) return;

            CrimsonConfig c = CrimsonClientClient.config;

            int sw = mc.getWindow().getGuiScaledWidth();
            int sh = mc.getWindow().getGuiScaledHeight();

            updateDragging(c, sw, sh);

            if (c.watermark) renderWatermark(g, c, sw);
            if (c.targetHud) renderTarget(g, c, sw, sh);
            if (c.armorHud) renderArmor(g, c, sw, sh);
            if (c.totemCounter) renderTotems(g, c, sw, sh);
            if (c.keystrokes) renderKeys(g, c);
            if (c.moduleList) renderModules(g, c);
            if (c.inventoryHud) renderInventory(g, c, sw, sh);
            if (c.potionHud) renderPotions(g, c, sw, sh);
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

    public static void onHit() {
        lastHitmarker = System.currentTimeMillis();
    }

    private static void drawText(
            GuiGraphics g,
            String value,
            int x,
            int y,
            int color,
            boolean shadow
    ) {
        Component text = Component.literal(value)
                .setStyle(Style.EMPTY.withFont(
                        new FontDescription.Resource(FONT)
                ));

        g.drawString(mc.font, text, x, y, color, shadow);
    }

    private static int x(int saved, int fallback) {
        return saved < 0 ? fallback : saved;
    }

    private static int y(int saved, int fallback) {
        return saved < 0 ? fallback : saved;
    }

    private static void renderWatermark(
            GuiGraphics g,
            CrimsonConfig c,
            int sw
    ) {
        int fps = mc.getFps();
        int ping = 0;

        if (mc.getConnection() != null) {
            var info = mc.getConnection()
                    .getPlayerInfo(mc.player.getUUID());

            if (info != null) {
                ping = info.getLatency();
            }
        }

        String brand = "CRIMSON";
        String stats = fps + " FPS  " + ping + " MS";

        int w = 145;
        int h = 27;

        int px = x(c.watermarkX, (sw - w) / 2);
        int py = y(c.watermarkY, 7);

        panel(g, px, py, w, h);

        g.blit(RenderPipelines.GUI_TEXTURED, LOGO,
                px + 5, py + 3, 0f, 0f, 22, 22, 128, 128);

        drawText(g, brand, px + 31, py + 5, 0xFFFFF3F5, true);
        drawText(g, stats, px + 31, py + 16, 0xFFB8B5C2, false);
    }

    private static void renderTarget(
            GuiGraphics g,
            CrimsonConfig c,
            int sw,
            int sh
    ) {
        Entity entity = mc.crosshairPickEntity;

        if (!(entity instanceof LivingEntity target) || !target.isAlive()) {
            return;
        }

        int w = 166;
        int h = 53;

        int px = x(c.targetHudX, sw / 2 + 20);
        int py = y(c.targetHudY, sh / 2 + 25);

        panel(g, px, py, w, h);
        panel(g, px + 7, py + 8, 34, 34);

        g.blit(RenderPipelines.GUI_TEXTURED, LOGO,
                px + 10, py + 11, 0f, 0f, 28, 28, 128, 128);

        String name = target.getName().getString();

        if (name.length() > 15) {
            name = name.substring(0, 12) + "...";
        }

        float hp = Math.max(0, target.getHealth());
        float maxHp = Math.max(1, target.getMaxHealth());
        float percent = Math.min(1f, hp / maxHp);

        String distance = String.format("%.1fm",
                mc.player.distanceTo(target));

        drawText(g, name, px + 48, py + 9, 0xFFF6F3F6, true);
        drawText(g, String.format("%.1f HP", hp),
                px + 48, py + 22, 0xFFFF7584, false);

        drawText(g, distance,
                px + w - mc.font.width(distance) - 8,
                py + 22, 0xFFAAA8B4, false);

        rounded(g, px + 48, py + 38, w - 57, 5, 2, 0xFF35212A);

        int fill = (int) ((w - 57) * percent);

        if (fill > 0) {
            rounded(g, px + 48, py + 38, fill, 5, 2, 0xFFFF4358);
        }
    }

    private static void renderArmor(
            GuiGraphics g,
            CrimsonConfig c,
            int sw,
            int sh
    ) {
        LocalPlayer p = mc.player;

        int px = x(c.armorHudX, sw - 27);
        int py = y(c.armorHudY, sh / 2 - 40);

        ItemStack[] armor = {
                p.getItemBySlot(EquipmentSlot.HEAD),
                p.getItemBySlot(EquipmentSlot.CHEST),
                p.getItemBySlot(EquipmentSlot.LEGS),
                p.getItemBySlot(EquipmentSlot.FEET)
        };

        for (int i = 0; i < armor.length; i++) {
            int iy = py + i * 20;

            rounded(g, px, iy, 20, 20, 5, 0xBB111521);

            if (!armor[i].isEmpty()) {
                g.renderItem(armor[i], px + 2, iy + 2);
                g.renderItemDecorations(mc.font, armor[i], px + 2, iy + 2);
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

        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);

            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                count += stack.getCount();
            }
        }

        if (count <= 0) return;

        int px = x(c.totemHudX, sw / 2 + 90);
        int py = y(c.totemHudY, sh - 52);

        panel(g, px, py, 43, 22);

        g.renderItem(new ItemStack(Items.TOTEM_OF_UNDYING), px + 2, py + 3);

        drawText(g, "x" + count, px + 23, py + 8,
                0xFFFFD66B, true);
    }

    private static void renderKeys(GuiGraphics g, CrimsonConfig c) {
        int px = c.keystrokesX;
        int py = c.keystrokesY;

        int s = 18;
        int gap = 2;

        key(g, px + s + gap, py, s, "W", mc.options.keyUp.isDown());
        key(g, px, py + s + gap, s, "A", mc.options.keyLeft.isDown());
        key(g, px + s + gap, py + s + gap, s, "S", mc.options.keyDown.isDown());
        key(g, px + 2 * (s + gap), py + s + gap, s, "D", mc.options.keyRight.isDown());
        key(g, px, py + 2 * (s + gap), 28, "LMB", mc.options.keyAttack.isDown());
        key(g, px + 30, py + 2 * (s + gap), 28, "RMB", mc.options.keyUse.isDown());
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

        drawText(g, label,
                x + (w - mc.font.width(label)) / 2,
                y + 5,
                down ? 0xFFFFFFFF : 0xFFD0CCD5,
                false);
    }

    private static void renderModules(GuiGraphics g, CrimsonConfig c) {
        List<String> list = new ArrayList<>();

        if (c.watermark) list.add("Watermark");
        if (c.targetHud) list.add("Target HUD");
        if (c.armorHud) list.add("Armor HUD");
        if (c.inventoryHud) list.add("Inventory HUD");
        if (c.potionHud) list.add("Potion HUD");
        if (c.keystrokes) list.add("Keystrokes");
        if (c.totemCounter) list.add("Totem Counter");
        if (c.targetEsp) list.add("Target ESP");
        if (c.hitSound) list.add("Hit Sound");
        if (c.customCrosshair) list.add("Crosshair");
        if (c.jumpCircle) list.add("Jump Circle");
        if (c.customHitbox) list.add("Hitbox");
        if (c.blockOverlay) list.add("Block Overlay");
        if (c.customSky) list.add("Custom Sky");
        if (c.noRender) list.add("No Render");
        if (c.fullbright) list.add("Fullbright");

        int px = c.moduleListX;
        int py = c.moduleListY;

        for (String name : list) {
            int w = mc.font.width(name) + 13;

            panel(g, px, py, w, 15);
            drawText(g, name, px + 6, py + 4, 0xFFFF7787, false);

            py += 17;
        }
    }

    private static void renderInventory(
            GuiGraphics g,
            CrimsonConfig c,
            int sw,
            int sh
    ) {
        int px = x(c.inventoryHudX, (sw - 180) / 2);
        int py = y(c.inventoryHudY, sh - 91);

        for (int i = 0; i < 9; i++) {
            int sx = px + i * 20;

            rounded(g, sx, py, 19, 19, 4, 0xBB111521);

            ItemStack stack = mc.player.getInventory().getItem(i + 9);

            if (!stack.isEmpty()) {
                g.renderItem(stack, sx + 1, py + 1);
                g.renderItemDecorations(mc.font, stack, sx + 1, py + 1);
            }
        }
    }

    private static void renderPotions(
            GuiGraphics g,
            CrimsonConfig c,
            int sw,
            int sh
    ) {
        int px = x(c.potionHudX, sw - 130);
        int py = c.potionHudY;

        for (var effect : mc.player.getActiveEffects()) {
            String name = effect.getEffect().value()
                    .getDisplayName().getString();

            int seconds = effect.getDuration() / 20;

            String line = name + " " +
                    (effect.getAmplifier() + 1) + " " +
                    (seconds / 60) + ":" +
                    String.format("%02d", seconds % 60);

            panel(g, px, py, 125, 16);
            drawText(g, line, px + 5, py + 4, 0xFFB7F5C4, false);

            py += 18;
        }
    }

    private static void renderDurability(
            GuiGraphics g,
            CrimsonConfig c,
            int sw,
            int sh
    ) {
        int px = x(c.durabilityHudX, sw / 2 + 10);
        int py = y(c.durabilityHudY, sh - 60);

        ItemStack main = mc.player.getMainHandItem();
        ItemStack off = mc.player.getOffhandItem();

        if (!main.isEmpty() && main.isDamageableItem()) {
            int left = main.getMaxDamage() - main.getDamageValue();

            drawText(g, left + "/" + main.getMaxDamage(),
                    px, py,
                    left < main.getMaxDamage() / 5
                            ? 0xFFFF4355 : 0xFFCCCCCC,
                    true);
        }

        if (!off.isEmpty() && off.isDamageableItem()) {
            int left = off.getMaxDamage() - off.getDamageValue();

            drawText(g, left + "/" + off.getMaxDamage(),
                    px, py + 12,
                    left < off.getMaxDamage() / 5
                            ? 0xFFFF4355 : 0xFFCCCCCC,
                    true);
        }
    }

    private static void renderCrosshair(GuiGraphics g, int sw, int sh) {
        CrimsonConfig c = CrimsonClientClient.config;
        int cx = sw / 2;
        int cy = sh / 2;

        int color = c.crosshairColor;
        float size = Math.max(2f, c.crosshairSize);
        float gap = Math.max(0f, c.crosshairGap);
        int s = Math.round(size);
        int gapI = Math.round(gap);

        switch (c.crosshairStyle) {
            case 1 -> {
                g.fill(cx - s - gapI, cy - 1, cx - gapI, cy + 1, color);
                g.fill(cx + gapI, cy - 1, cx + s + gapI, cy + 1, color);
                g.fill(cx - 1, cy - s - gapI, cx + 1, cy - gapI, color);
                g.fill(cx - 1, cy + gapI, cx + 1, cy + s + gapI, color);
            }
            case 2 -> {
                g.fill(cx - s - gapI, cy - 1, cx + s + gapI + 1, cy + 1, color);
                g.fill(cx - 1, cy + 1, cx + 1, cy + s + gapI + 1, color);
            }
            case 3 -> {
                for (int i = 0; i <= s; i++) {
                    g.fill(cx - i, cy - (s - i), cx - i + 1, cy - (s - i) + 1, color);
                    g.fill(cx + i, cy - (s - i), cx + i + 1, cy - (s - i) + 1, color);
                    g.fill(cx - i, cy + (s - i), cx - i + 1, cy + (s - i) + 1, color);
                    g.fill(cx + i, cy + (s - i), cx + i + 1, cy + (s - i) + 1, color);
                }
            }
            default -> {
                g.fill(cx - s - gapI, cy - 1, cx - gapI, cy + 1, color);
                g.fill(cx + gapI, cy - 1, cx + s + gapI + 1, cy + 1, color);
                g.fill(cx - 1, cy - s - gapI, cx + 1, cy - gapI, color);
                g.fill(cx - 1, cy + gapI, cx + 1, cy + s + gapI + 1, color);
                g.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFFFFFFFF);
            }
        }
    }

    private static void renderHitmarker(GuiGraphics g, int sw, int sh) {
        int cx = sw / 2;
        int cy = sh / 2;

        int s = 8;
        int color = 0xAAFF4355;

        g.fill(cx - s, cy - s, cx - s + 2, cy - s + 2, color);
        g.fill(cx + s - 2, cy - s, cx + s, cy - s + 2, color);
        g.fill(cx - s, cy + s - 2, cx - s + 2, cy + s, color);
        g.fill(cx + s - 2, cy + s - 2, cx + s, cy + s, color);
    }

    private static void updateDragging(
            CrimsonConfig c,
            int sw,
            int sh
    ) {
        if (!(mc.screen instanceof ChatScreen)) {
            dragging = null;
            wasMouseDown = false;
            return;
        }

        long window = mc.getWindow().handle();

        boolean down = GLFW.glfwGetMouseButton(
                window,
                GLFW.GLFW_MOUSE_BUTTON_LEFT
        ) == GLFW.GLFW_PRESS;

        double mouseX = mc.mouseHandler.xpos()
                * sw / mc.getWindow().getScreenWidth();

        double mouseY = mc.mouseHandler.ypos()
                * sh / mc.getWindow().getScreenHeight();

        if (down && !wasMouseDown) {
            HudBox box = findBox(c, sw, sh,
                    (int) mouseX, (int) mouseY);

            if (box != null) {
                dragging = box.id;
                dragOffsetX = mouseX - box.x;
                dragOffsetY = mouseY - box.y;
            }
        }

        if (down && dragging != null) {
            int nx = (int) (mouseX - dragOffsetX);
            int ny = (int) (mouseY - dragOffsetY);

            setPosition(c, dragging, nx, ny);
        }

        if (!down && wasMouseDown && dragging != null) {
            c.save();
            dragging = null;
        }

        wasMouseDown = down;
    }

    private static HudBox findBox(
            CrimsonConfig c,
            int sw,
            int sh,
            int mx,
            int my
    ) {
        List<HudBox> boxes = new ArrayList<>();

        if (c.watermark) {
            boxes.add(new HudBox("watermark",
                    x(c.watermarkX, (sw - 145) / 2),
                    c.watermarkY, 145, 27));
        }

        if (c.targetHud) {
            boxes.add(new HudBox("target",
                    x(c.targetHudX, sw / 2 + 20),
                    y(c.targetHudY, sh / 2 + 25), 166, 53));
        }

        if (c.armorHud) {
            boxes.add(new HudBox("armor",
                    x(c.armorHudX, sw - 27),
                    y(c.armorHudY, sh / 2 - 40), 20, 80));
        }

        if (c.totemCounter) {
            boxes.add(new HudBox("totem",
                    x(c.totemHudX, sw / 2 + 90),
                    y(c.totemHudY, sh - 52), 43, 22));
        }

        if (c.keystrokes) {
            boxes.add(new HudBox("keys",
                    c.keystrokesX, c.keystrokesY, 58, 58));
        }

        if (c.moduleList) {
            boxes.add(new HudBox("modules",
                    c.moduleListX, c.moduleListY, 115, 120));
        }

        if (c.inventoryHud) {
            boxes.add(new HudBox("inventory",
                    x(c.inventoryHudX, (sw - 180) / 2),
                    y(c.inventoryHudY, sh - 91), 180, 19));
        }

        if (c.potionHud) {
            boxes.add(new HudBox("potions",
                    x(c.potionHudX, sw - 130),
                    c.potionHudY, 125, 90));
        }

        if (c.durabilityHud) {
            boxes.add(new HudBox("durability",
                    x(c.durabilityHudX, sw / 2 + 10),
                    y(c.durabilityHudY, sh - 60), 70, 25));
        }

        for (int i = boxes.size() - 1; i >= 0; i--) {
            HudBox b = boxes.get(i);

            if (mx >= b.x && mx <= b.x + b.w &&
                    my >= b.y && my <= b.y + b.h) {
                return b;
            }
        }

        return null;
    }

    private static void setPosition(
            CrimsonConfig c,
            String id,
            int x,
            int y
    ) {
        switch (id) {
            case "watermark" -> {
                c.watermarkX = x;
                c.watermarkY = y;
            }
            case "target" -> {
                c.targetHudX = x;
                c.targetHudY = y;
            }
            case "armor" -> {
                c.armorHudX = x;
                c.armorHudY = y;
            }
            case "totem" -> {
                c.totemHudX = x;
                c.totemHudY = y;
            }
            case "keys" -> {
                c.keystrokesX = x;
                c.keystrokesY = y;
            }
            case "modules" -> {
                c.moduleListX = x;
                c.moduleListY = y;
            }
            case "inventory" -> {
                c.inventoryHudX = x;
                c.inventoryHudY = y;
            }
            case "potions" -> {
                c.potionHudX = x;
                c.potionHudY = y;
            }
            case "durability" -> {
                c.durabilityHudX = x;
                c.durabilityHudY = y;
            }
        }
    }

    private static void drawEditorHint(GuiGraphics g, int sw) {
        String hint = dragging == null
                ? "CRIMSON HUD EDITOR | Drag HUD elements with LMB"
                : "MOVING: " + dragging.toUpperCase();

        int w = mc.font.width(hint) + 18;
        int x = (sw - w) / 2;
        int y = 38;

        panel(g, x, y, w, 20);
        drawText(g, hint, x + 9, y + 6, 0xFFFF7585, true);
    }

    private record HudBox(String id, int x, int y, int w, int h) {}

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
            int r,
            int color
    ) {
        if (w <= 0 || h <= 0) return;

        r = Math.max(1, Math.min(r, Math.min(w / 2, h / 2)));

        for (int row = 0; row < r; row++) {
            int inset = (int) Math.ceil(
                    r - Math.sqrt(r * r - (r - row) * (r - row))
            );

            g.fill(x + inset, y + row,
                    x + w - inset, y + row + 1, color);

            g.fill(x + inset, y + h - row - 1,
                    x + w - inset, y + h - row, color);
        }

        g.fill(x, y + r, x + w, y + h - r, color);
    }
}
