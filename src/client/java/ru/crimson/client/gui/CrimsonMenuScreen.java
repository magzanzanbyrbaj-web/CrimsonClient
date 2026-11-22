package ru.crimson.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CrimsonMenuScreen extends Screen {

    private static final Identifier FONT =
            Identifier.fromNamespaceAndPath("crimsonclient", "inter");

    private static final int BG = 0x99000000;
    private static final int PANEL = 0xE51D1D21;
    private static final int PANEL_HOVER = 0xFF29292F;
    private static final int ROW = 0x55202025;
    private static final int ROW_HOVER = 0xAA34343C;

    private static final int ACCENT = 0xFF8798F5;
    private static final int ACCENT_DARK = 0xFF5969B8;

    private static final int WHITE = 0xFFF0F0F5;
    private static final int MUTED = 0xFFB3B3BE;
    private static final int DIM = 0xFF777782;
    private static final int BORDER = 0x33202028;

    private static final String[] CATEGORIES = {
            "Combat", "Movement", "Render", "Player", "Misc"
    };

    private static final int PANEL_GAP = 12;
    private static final int ROW_HEIGHT = 37;

    private final CrimsonConfig cfg;
    private final List<Feature> features = new ArrayList<>();
    private final int[] scroll = new int[5];

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int columnW;

    private boolean waitingForBind;
    private Feature bindingFeature;

    public CrimsonMenuScreen() {
        super(Component.literal("Crimson Client"));
        cfg = CrimsonClientClient.config;

        if (cfg != null) {
            buildFeatures();
        }
    }

    private void buildFeatures() {

        // COMBAT — визуальные эффекты боя
        add("HitParticles", "Hit particles", 0,
                () -> cfg.hitParticles, v -> cfg.hitParticles = v);

        add("CriticalEffects", "Critical effects", 0,
                () -> cfg.criticalEffects, v -> cfg.criticalEffects = v);

        add("HitSound", "Hit sound", 0,
                () -> cfg.hitSound, v -> cfg.hitSound = v);

        add("Hitmarker", "Hitmarker", 0,
                () -> cfg.hitmarker, v -> cfg.hitmarker = v);

        add("DamageNumbers", "Damage numbers", 0,
                () -> cfg.damageNumbers, v -> cfg.damageNumbers = v);

        // MOVEMENT — визуальные эффекты движения
        add("Trails", "Movement trails", 1,
                () -> cfg.trails, v -> cfg.trails = v);

        add("JumpCircle", "Jump circle", 1,
                () -> cfg.jumpCircle, v -> cfg.jumpCircle = v);

        add("SmoothSword", "Smooth sword", 1,
                () -> cfg.smoothSword, v -> cfg.smoothSword = v);

        add("ItemSwitch", "Item switch animation", 1,
                () -> cfg.itemSwitchAnim, v -> cfg.itemSwitchAnim = v);

        // RENDER — графика и визуальные эффекты
        add("CustomCrosshair", "Custom crosshair", 2,
                () -> cfg.customCrosshair, v -> cfg.customCrosshair = v);

        add("CustomSky", "Custom sky", 2,
                () -> cfg.customSky, v -> cfg.customSky = v);

        add("ChinaHat", "China Hat", 2,
                () -> cfg.chinaHat, v -> cfg.chinaHat = v);

        add("BlockOverlay", "Block overlay", 2,
                () -> cfg.blockOverlay, v -> cfg.blockOverlay = v);

        add("NoRender", "No Render", 2,
                () -> cfg.noRender, v -> cfg.noRender = v);

        add("InventoryBlur", "Inventory blur", 2,
                () -> cfg.inventoryBlur, v -> cfg.inventoryBlur = v);

        add("TotemAnimation", "Totem animation", 2,
                () -> cfg.totemAnimation, v -> cfg.totemAnimation = v);

        add("Fullbright", "Full Bright", 2,
                () -> cfg.fullbright, v -> cfg.fullbright = v);

        add("ReduceParticles", "Reduce particles", 2,
                () -> cfg.reduceParticles, v -> cfg.reduceParticles = v);

        // PLAYER — HUD и информация
        add("TargetHUD", "Target HUD", 3,
                () -> cfg.targetHud, v -> cfg.targetHud = v);

        add("ArmorHUD", "Armor HUD", 3,
                () -> cfg.armorHud, v -> cfg.armorHud = v);

        add("TotemCounter", "Totem counter", 3,
                () -> cfg.totemCounter, v -> cfg.totemCounter = v);

        add("InventoryHUD", "Inventory HUD", 3,
                () -> cfg.inventoryHud, v -> cfg.inventoryHud = v);

        add("PotionHUD", "Potion HUD", 3,
                () -> cfg.potionHud, v -> cfg.potionHud = v);

        add("DurabilityHUD", "Durability HUD", 3,
                () -> cfg.durabilityHud, v -> cfg.durabilityHud = v);

        add("Keystrokes", "Keystrokes", 3,
                () -> cfg.keystrokes, v -> cfg.keystrokes = v);

        // MISC — интерфейс и производительность
        add("Watermark", "Watermark", 4,
                () -> cfg.watermark, v -> cfg.watermark = v);

        add("ModuleList", "Module list", 4,
                () -> cfg.moduleList, v -> cfg.moduleList = v);

        add("FPSBoost", "FPS Boost", 4,
                () -> cfg.fpsBoost, v -> cfg.fpsBoost = v);

        add("Optimization", "Optimization", 4,
                () -> cfg.optimized, v -> cfg.optimized = v);

        add("MenuAnimation", "Menu animation", 4,
                () -> cfg.glassAnimation, v -> cfg.glassAnimation = v);
    }

    private void add(String name, String description, int category,
                     BooleanSupplier getter, Consumer<Boolean> setter) {
        features.add(new Feature(
                name, description, category, getter, setter
        ));
    }

    private void calculateLayout() {
        panelW = Math.min(width - 28, 1500);
        panelH = Math.min(height - 28, 680);

        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        columnW = (panelW - 32 - PANEL_GAP * 4) / 5;
    }

    @Override
    protected void init() {
        calculateLayout();
    }

    private void drawText(GuiGraphics g, String value,
                          int x, int y, int color) {
        Component component = Component.literal(value)
                .setStyle(Style.EMPTY.withFont(
                        new FontDescription.Resource(FONT)
                ));

        g.drawString(font, component, x, y, color, false);
    }

    private void rounded(GuiGraphics g, int x, int y,
                         int w, int h, int radius, int color) {
        if (w <= 0 || h <= 0) return;

        radius = Math.max(1, Math.min(
                radius, Math.min(w / 2, h / 2)
        ));

        for (int i = 0; i < radius; i++) {
            int inset = (int) Math.ceil(
                    radius - Math.sqrt(
                            radius * radius
                                    - (radius - i) * (radius - i)
                    )
            );

            g.fill(x + inset, y + i,
                    x + w - inset, y + i + 1, color);

            g.fill(x + inset, y + h - i - 1,
                    x + w - inset, y + h - i, color);
        }

        g.fill(x, y + radius, x + w, y + h - radius, color);
    }

    private boolean inside(int mx, int my,
                           int x, int y, int w, int h) {
        return mx >= x && mx <= x + w
                && my >= y && my <= y + h;
    }

    private int getColumnX(int category) {
        return panelX + 8 + category * (columnW + PANEL_GAP);
    }

    private List<Feature> getFeatures(int category) {
        List<Feature> result = new ArrayList<>();

        for (Feature feature : features) {
            if (feature.category == category) {
                result.add(feature);
            }
        }

        return result;
    }

    private void drawBackground(GuiGraphics g) {
        g.fill(0, 0, width, height, BG);

        rounded(g,
                panelX + 3, panelY + 5,
                panelW, panelH,
                16, 0x55000000
        );

        rounded(g,
                panelX, panelY,
                panelW, panelH,
                16, 0x44202025
        );
    }

    private void drawHeader(GuiGraphics g) {
        String title = "CRIMSON CLIENT";
        String subtitle = "VISUAL MODULES";

        int titleWidth = font.width(title);
        int subtitleWidth = font.width(subtitle);

        drawText(g, title,
                width / 2 - titleWidth / 2,
                panelY - 17,
                WHITE
        );

        drawText(g, subtitle,
                width / 2 - subtitleWidth / 2,
                panelY - 5,
                MUTED
        );

        int buttonW = 78;
        int buttonH = 23;
        int buttonX = panelX + panelW - buttonW - 12;
        int buttonY = panelY + 10;

        rounded(g, buttonX, buttonY,
                buttonW, buttonH, 7, 0xAA292930);

        drawText(g, "CONFIGS",
                buttonX + 12, buttonY + 7,
                MUTED
        );
    }

    private void drawColumn(GuiGraphics g, int category,
                            int mouseX, int mouseY) {
        int x = getColumnX(category);
        int y = panelY + 10;
        int h = panelH - 20;

        boolean panelHover = inside(
                mouseX, mouseY, x, y, columnW, h
        );

        rounded(g, x, y, columnW, h, 16,
                panelHover ? 0xEE25252A : PANEL
        );

        g.fill(x + 1, y + 1,
                x + columnW - 1, y + 2,
                BORDER
        );

        drawText(g, CATEGORIES[category],
                x + 17, y + 17, WHITE
        );

        drawCategoryIcon(g, category,
                x + columnW - 28, y + 16
        );

        g.fill(x + 12, y + 42,
                x + columnW - 12, y + 43,
                0x33202028
        );

        List<Feature> list = getFeatures(category);

        int startY = y + 53;
        int bottom = y + h - 12;
        int visibleRows = Math.max(
                1, (bottom - startY) / ROW_HEIGHT
        );

        int maxScroll = Math.max(
                0, list.size() - visibleRows
        );

        scroll[category] = Mth.clamp(
                scroll[category], 0, maxScroll
        );

        int end = Math.min(
                list.size(), scroll[category] + visibleRows
        );

        for (int i = scroll[category]; i < end; i++) {
            Feature feature = list.get(i);

            int rowY = startY
                    + (i - scroll[category]) * ROW_HEIGHT;

            drawFeature(g, feature,
                    x + 9, rowY,
                    columnW - 18,
                    mouseX, mouseY
            );
        }

        if (maxScroll > 0) {
            drawText(g,
                    (scroll[category] + 1) + "/" + (maxScroll + 1),
                    x + columnW - 42,
                    y + h - 17,
                    DIM
            );
        }
    }

    private void drawCategoryIcon(GuiGraphics g, int category,
                                  int x, int y) {
        String icon = switch (category) {
            case 0 -> "⚔";
            case 1 -> "➤";
            case 2 -> "▣";
            case 3 -> "♟";
            default -> "✦";
        };

        drawText(g, icon, x, y, MUTED);
    }

    private void drawFeature(GuiGraphics g, Feature feature,
                             int x, int y, int w,
                             int mouseX, int mouseY) {
        boolean enabled = feature.enabled.getAsBoolean();

        boolean hover = inside(
                mouseX, mouseY, x, y, w, ROW_HEIGHT - 3
        );

        if (hover) {
            rounded(g, x, y, w, ROW_HEIGHT - 3,
                    8, ROW_HOVER);
        }

        drawText(g, feature.name,
                x + 10, y + 12,
                enabled ? WHITE : MUTED
        );

        // Три точки справа
        drawText(g, "...",
                x + w - 44, y + 12,
                DIM
        );

        // Переключатель
        int switchX = x + w - 29;
        int switchY = y + 9;

        rounded(g, switchX, switchY,
                20, 14, 7,
                enabled ? ACCENT_DARK : 0xFF414149
        );

        int circleX = enabled
                ? switchX + 10
                : switchX + 2;

        rounded(g, circleX, switchY + 2,
                10, 10, 5,
                enabled ? WHITE : 0xFFAAAAAF
        );

        if (hover) {
            drawText(g, feature.description,
                    x + 10, y + 25, DIM
            );
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX,
                       int mouseY, float partialTick) {
        calculateLayout();

        drawBackground(g);
        drawHeader(g);

        for (int i = 0; i < CATEGORIES.length; i++) {
            drawColumn(g, i, mouseX, mouseY);
        }

        if (waitingForBind && bindingFeature != null) {
            String message = "PRESS A KEY TO BIND  |  DELETE - CLEAR  |  ESC - CANCEL";

            int w = font.width(message) + 24;
            int x = (width - w) / 2;
            int y = panelY + panelH - 31;

            rounded(g, x, y, w, 23,
                    7, 0xEE25252D);

            drawText(g, message,
                    x + 12, y + 7, ACCENT
            );
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private Feature getFeatureAt(int mx, int my) {
        for (int category = 0; category < 5; category++) {
            int x = getColumnX(category);
            int y = panelY + 10;
            int startY = y + 53;
            int bottom = y + panelH - 22;

            int visibleRows = Math.max(
                    1, (bottom - startY) / ROW_HEIGHT
            );

            List<Feature> list = getFeatures(category);

            int end = Math.min(
                    list.size(),
                    scroll[category] + visibleRows
            );

            for (int i = scroll[category]; i < end; i++) {
                int rowY = startY
                        + (i - scroll[category]) * ROW_HEIGHT;

                if (inside(mx, my,
                        x + 9, rowY,
                        columnW - 18, ROW_HEIGHT - 3)) {
                    return list.get(i);
                }
            }
        }

        return null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event,
                                boolean doubleClick) {
        int mx = (int) event.x();
        int my = (int) event.y();

        if (waitingForBind) {
            return true;
        }

        int buttonX = panelX + panelW - 78 - 12;
        int buttonY = panelY + 10;

        if (event.button() == 0
                && inside(mx, my, buttonX, buttonY, 78, 23)) {
            Minecraft.getInstance().setScreen(
                    new ConfigsScreen(this)
            );
            return true;
        }

        Feature feature = getFeatureAt(mx, my);

        if (feature != null) {
            if (event.button() == 0) {
                feature.setter.accept(
                        !feature.enabled.getAsBoolean()
                );

                cfg.save();
                return true;
            }

            if (event.button() == 1) {
                if (getBind(feature.name) != Integer.MIN_VALUE) {
                    waitingForBind = true;
                    bindingFeature = feature;
                }

                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double deltaX, double deltaY) {
        for (int category = 0; category < 5; category++) {
            int x = getColumnX(category);
            int y = panelY + 10;

            if (inside((int) mouseX, (int) mouseY,
                    x, y, columnW, panelH - 20)) {
                int maxScroll = Math.max(
                        0,
                        getFeatures(category).size() - 1
                );

                scroll[category] = Mth.clamp(
                        scroll[category] + (deltaY < 0 ? 1 : -1),
                        0, maxScroll
                );

                return true;
            }
        }

        return super.mouseScrolled(
                mouseX, mouseY, deltaX, deltaY
        );
    }

    private int getBind(String feature) {
        return switch (feature) {
            case "TargetHUD" -> cfg.bindTargetHUD;
            case "ArmorHUD" -> cfg.bindArmorHUD;
            case "TotemCounter" -> cfg.bindTotemCounter;
            case "Watermark" -> cfg.bindWatermark;
            case "Keystrokes" -> cfg.bindKeystrokes;
            case "ModuleList" -> cfg.bindModuleList;
            case "InventoryHUD" -> cfg.bindInventoryHUD;
            case "PotionHUD" -> cfg.bindPotionHUD;
            case "DurabilityHUD" -> cfg.bindDurabilityHUD;
            default -> Integer.MIN_VALUE;
        };
    }

    private void setBind(String feature, int key) {
        switch (feature) {
            case "TargetHUD" -> cfg.bindTargetHUD = key;
            case "ArmorHUD" -> cfg.bindArmorHUD = key;
            case "TotemCounter" -> cfg.bindTotemCounter = key;
            case "Watermark" -> cfg.bindWatermark = key;
            case "Keystrokes" -> cfg.bindKeystrokes = key;
            case "ModuleList" -> cfg.bindModuleList = key;
            case "InventoryHUD" -> cfg.bindInventoryHUD = key;
            case "PotionHUD" -> cfg.bindPotionHUD = key;
            case "DurabilityHUD" -> cfg.bindDurabilityHUD = key;
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();

        if (waitingForBind && bindingFeature != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                waitingForBind = false;
                bindingFeature = null;
                return true;
            }

            if (key == GLFW.GLFW_KEY_DELETE
                    || key == GLFW.GLFW_KEY_BACKSPACE) {
                setBind(bindingFeature.name, -1);
            } else {
                setBind(bindingFeature.name, key);
            }

            waitingForBind = false;
            bindingFeature = null;

            cfg.save();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        if (cfg != null) {
            cfg.save();
        }

        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record Feature(
            String name,
            String description,
            int category,
            BooleanSupplier enabled,
            Consumer<Boolean> setter
    ) {}
            }
