
package ru.crimson.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CrimsonMenuScreen extends Screen {
    private static final int RED = 0xFFFF3548;
    private static final int WHITE = 0xFFF4F2F5;
    private static final int MUTED = 0xFF9B98A5;

    private static final String[] CATEGORIES = {
            "Combat", "Movement", "Player", "Visuals", "Client", "Configs"
    };

    private final CrimsonConfig config;
    private final long openedAt;
    private int selectedCategory = 3;
    private float animation = 0;

    private final List<Feature> features = new ArrayList<>();

    private int panelX, panelY, panelW, panelH;
    private int sidebarW = 142;
    private int contentX, contentY, contentW;

    public CrimsonMenuScreen() {
        super(Component.literal("Crimson Client"));
        config = CrimsonClientClient.config;
        openedAt = System.currentTimeMillis();
        buildFeatures();
    }

    private void buildFeatures() {
        features.clear();

        add("Watermark", "FPS, ping and client branding", () -> config.watermark, v -> config.watermark = v, 4);
        add("Target HUD", "Target information and health bar", () -> config.targetHud, v -> config.targetHud = v, 0);
        add("Armor HUD", "Armor equipment display", () -> config.armorHud, v -> config.armorHud = v, 2);
        add("Totem Counter", "Counts totems in inventory", () -> config.totemCounter, v -> config.totemCounter = v, 2);
        add("Keystrokes", "Keyboard and mouse display", () -> config.keystrokes, v -> config.keystrokes = v, 4);
        add("Module List", "Enabled modules on screen", () -> config.moduleList, v -> config.moduleList = v, 4);
        add("Inventory HUD", "Inventory items display", () -> config.inventoryHud, v -> config.inventoryHud = v, 2);
        add("Potion HUD", "Active potion effects", () -> config.potionHud, v -> config.potionHud = v, 2);
        add("Durability HUD", "Item durability information", () -> config.durabilityHud, v -> config.durabilityHud = v, 2);

        add("Hit Particles", "Visual hit particle effects", () -> config.hitParticles, v -> config.hitParticles = v, 0);
        add("Damage Numbers", "Damage number display", () -> config.damageNumbers, v -> config.damageNumbers = v, 0);
        add("Hitmarker", "Visual hit confirmation", () -> config.hitmarker, v -> config.hitmarker = v, 0);
        add("Critical Effects", "Critical hit visuals", () -> config.criticalEffects, v -> config.criticalEffects = v, 0);

        add("Smooth Sword", "Smoother sword animation", () -> config.smoothSword, v -> config.smoothSword = v, 3);
        add("Item Switch Anim", "Item switching animation", () -> config.itemSwitchAnim, v -> config.itemSwitchAnim = v, 3);
        add("Totem Animation", "Totem animation effects", () -> config.totemAnimation, v -> config.totemAnimation = v, 3);
        add("Custom Crosshair", "Custom crosshair style", () -> config.customCrosshair, v -> config.customCrosshair = v, 3);
        add("Custom Sky", "Custom sky visuals", () -> config.customSky, v -> config.customSky = v, 3);
        add("Inventory Blur", "Blur behind inventory", () -> config.inventoryBlur, v -> config.inventoryBlur = v, 3);

        add("Fullbright", "Increased world brightness", () -> config.fullbright, v -> config.fullbright = v, 4);
        add("Reduce Particles", "Reduce particle rendering", () -> config.reduceParticles, v -> config.reduceParticles = v, 4);
        add("Optimization", "Client optimization mode", () -> config.optimized, v -> config.optimized = v, 4);
        add("Menu Animation", "Animated menu transitions", () -> config.glassAnimation, v -> config.glassAnimation = v, 4);
    }

    private void add(String name, String description, BooleanSupplier getter,
                     Consumer<Boolean> setter, int category) {
        features.add(new Feature(name, description, getter, setter, category));
    }

    private void calculateLayout() {
        panelW = Math.min(620, width - 24);
        panelH = Math.min(390, height - 24);

        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        sidebarW = Math.min(145, panelW / 3);
        contentX = panelX + sidebarW + 18;
        contentY = panelY + 54;
        contentW = panelW - sidebarW - 34;
    }

    @Override
    protected void init() {
        calculateLayout();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        calculateLayout();

        float t = Mth.clamp((System.currentTimeMillis() - openedAt) / 260f, 0f, 1f);
        animation = config.glassAnimation ? 1f - (float) Math.pow(1f - t, 3f) : 1f;

        g.fill(0, 0, width, height, 0x66030710);

        int lift = (int) ((1f - animation) * 12);
        int py = panelY + lift;

        rounded(g, panelX + 5, py + 7, panelW, panelH, 17, 0x70000000);
        rounded(g, panelX, py, panelW, panelH, 17, alpha(0xE5111420));
        rounded(g, panelX + 1, py + 1, panelW - 2, panelH - 2, 16, alpha(0xD91A1D2B));

        rounded(g, panelX + 1, py + 1, sidebarW, panelH - 2, 15, alpha(0xB90B0E17));
        rounded(g, panelX + 16, py + 1, panelW - 32, 2, 1, alpha(RED));

        g.fill(panelX + sidebarW, py + 18, panelX + sidebarW + 1,
                py + panelH - 18, alpha(0x33FFFFFF));

        drawSidebar(g, mouseX, mouseY, py);
        drawHeader(g, py);
        drawFeatures(g, mouseX, mouseY, py);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawSidebar(GuiGraphics g, int mx, int my, int py) {
        int sx = panelX + 13;
        int sy = py + 17;

        g.drawString(font, "C", sx + 3, sy + 1, alpha(RED), true);
        g.drawString(font, "CRIMSON", sx + 20, sy, alpha(WHITE), true);
        g.drawString(font, "CLIENT", sx + 20, sy + 11, alpha(MUTED), false);

        int y = sy + 43;

        for (int i = 0; i < CATEGORIES.length; i++) {
            int bx = panelX + 9;
            int by = y + i * 34;
            int bw = sidebarW - 18;
            int bh = 27;

            boolean selected = selectedCategory == i;
            boolean hover = inside(mx, my, bx, by, bw, bh);

            if (selected) {
                rounded(g, bx, by, bw, bh, 7, alpha(0xB33D202D));
                rounded(g, bx, by, 3, bh, 2, alpha(RED));
            } else if (hover) {
                rounded(g, bx, by, bw, bh, 7, alpha(0x553B3D4B));
            }

            String symbol = switch (i) {
                case 0 -> "⚔";
                case 1 -> "↗";
                case 2 -> "◆";
                case 3 -> "◈";
                case 4 -> "⚙";
                default -> "▣";
            };

            g.drawString(font, symbol, bx + 10, by + 9,
                    alpha(selected ? RED : MUTED), false);

            g.drawString(font, CATEGORIES[i], bx + 27, by + 9,
                    alpha(selected ? WHITE : MUTED), false);

            int count = countCategory(i);
            String amount = String.valueOf(count);
            g.drawString(font, amount, bx + bw - font.width(amount) - 8,
                    by + 9, alpha(MUTED), false);
        }

        int profileY = py + panelH - 49;
        rounded(g, panelX + 9, profileY, sidebarW - 18, 37, 8, alpha(0x553A3D4D));
        rounded(g, panelX + 15, profileY + 7, 23, 23, 6, alpha(0xFF542A39));
        g.drawString(font, "C", panelX + 23, profileY + 15, alpha(WHITE), true);

        String playerName = Minecraft.getInstance().player == null
                ? "Player" : Minecraft.getInstance().player.getName().getString();

        g.drawString(font, trim(playerName, 13), panelX + 44,
                profileY + 8, alpha(WHITE), false);
        g.drawString(font, "Crimson User", panelX + 44,
                profileY + 20, alpha(MUTED), false);
    }

    private void drawHeader(GuiGraphics g, int py) {
        String title = CATEGORIES[selectedCategory];
        g.drawString(font, title, contentX, py + 20, alpha(WHITE), true);
        g.drawString(font, "Manage your client modules",
                contentX, py + 34, alpha(MUTED), false);

        int searchW = Math.min(160, contentW / 2);
        int searchX = panelX + panelW - searchW - 17;

        rounded(g, searchX, py + 16, searchW, 25, 7, alpha(0x77383D4C));
        g.drawString(font, "⌕  Search modules...", searchX + 9,
                py + 25, alpha(MUTED), false);
    }

    private void drawFeatures(GuiGraphics g, int mx, int my, int py) {
        List<Feature> visible = new ArrayList<>();

        for (Feature feature : features) {
            if (feature.category == selectedCategory) visible.add(feature);
        }

        if (selectedCategory == 5) {
            g.drawString(font, "CONFIGURATION", contentX, py + 75, alpha(WHITE), true);
            g.drawString(font, "Your settings are saved automatically.",
                    contentX, py + 94, alpha(MUTED), false);
            return;
        }

        int columns = contentW >= 330 ? 2 : 1;
        int gap = 10;
        int cardW = (contentW - gap * (columns - 1)) / columns;
        int cardH = 66;

        for (int i = 0; i < visible.size(); i++) {
            Feature feature = visible.get(i);

            int col = i % columns;
            int row = i / columns;

            int x = contentX + col * (cardW + gap);
            int y = py + 65 + row * (cardH + 10);

            boolean enabled = feature.enabled.getAsBoolean();
            boolean hover = inside(mx, my, x, y, cardW, cardH);

            rounded(g, x, y, cardW, cardH, 9,
                    alpha(enabled ? 0x713C202B : 0x66323848));

            rounded(g, x, y, cardW, 1, 1,
                    alpha(enabled ? 0xAAFF5365 : 0x446F7587));

            g.drawString(font, feature.name, x + 12, y + 13,
                    alpha(WHITE), true);

            g.drawString(font, trim(feature.description, 27),
                    x + 12, y + 29, alpha(MUTED), false);

            int sw = 29;
            int sh = 15;
            int tx = x + cardW - sw - 12;
            int ty = y + 12;

            rounded(g, tx, ty, sw, sh, 8,
                    alpha(enabled ? 0xFFFF4355 : 0xFF555A68));

            int knobX = enabled ? tx + sw - 12 : tx + 3;
            rounded(g, knobX, ty + 3, 9, 9, 5, alpha(WHITE));

            if (hover) {
                rounded(g, x, y + cardH - 2, cardW, 2, 1, alpha(0x66FF4355));
            }
        }
    }

    private int countCategory(int category) {
        int count = 0;
        for (Feature feature : features) {
            if (feature.category == category) count++;
        }
        return count;
    }

    private boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private String trim(String value, int max) {
        if (value.length() <= max) return value;
        return value.substring(0, Math.max(0, max - 3)) + "...";
    }

    private int alpha(int color) {
        int a = color >>> 24;
        return ((int) (a * animation) << 24) | (color & 0x00FFFFFF);
    }

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

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int mx = (int) event.x();
            int my = (int) event.y();

            for (int i = 0; i < CATEGORIES.length; i++) {
                int bx = panelX + 9;
                int by = panelY + 17 + 43 + i * 34;

                if (inside(mx, my, bx, by, sidebarW - 18, 27)) {
                    selectedCategory = i;
                    return true;
                }
            }

            List<Feature> visible = new ArrayList<>();
            for (Feature feature : features) {
                if (feature.category == selectedCategory) visible.add(feature);
            }

            int columns = contentW >= 330 ? 2 : 1;
            int gap = 10;
            int cardW = (contentW - gap * (columns - 1)) / columns;
            int cardH = 66;

            for (int i = 0; i < visible.size(); i++) {
                int col = i % columns;
                int row = i / columns;
                int x = contentX + col * (cardW + gap);
                int y = panelY + 65 + row * (cardH + 10);

                if (inside(mx, my, x, y, cardW, cardH)) {
                    Feature feature = visible.get(i);
                    feature.setter.accept(!feature.enabled.getAsBoolean());
                    config.save();
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class Feature {
        final String name;
        final String description;
        final BooleanSupplier enabled;
        final Consumer<Boolean> setter;
        final int category;

        Feature(String name, String description, BooleanSupplier enabled,
                Consumer<Boolean> setter, int category) {
            this.name = name;
            this.description = description;
            this.enabled = enabled;
            this.setter = setter;
            this.category = category;
        }
    }
}
