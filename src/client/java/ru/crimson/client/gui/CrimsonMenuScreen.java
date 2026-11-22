
package ru.crimson.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
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
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class CrimsonMenuScreen extends Screen {

    private static final Identifier FONT =
            Identifier.fromNamespaceAndPath("crimsonclient", "inter");

    private static final Identifier LOGO =
            Identifier.fromNamespaceAndPath(
                    "crimsonclient", "crimson_logo.png"
            );

    private static final int ACCENT = 0xFFFF465F;
    private static final int WHITE = 0xFFF3F3F7;
    private static final int MUTED = 0xFF9999A5;
    private static final int PANEL = 0xEE171820;
    private static final int ROW_HOVER = 0xFF292B36;

    private static final String[] CATEGORIES = {
            "Combat", "Movement", "Render", "Player", "Misc"
    };

    private static final int ROW_HEIGHT = 36;
    private static final int GAP = 10;
    private static final int HEADER_HEIGHT = 48;

    private final CrimsonConfig cfg;
    private final List<Feature> features = new ArrayList<>();
    private final int[] scroll = new int[5];

    private int panelX, panelY, panelW, panelH, columnW;

    private boolean waitingForBind;
    private Feature bindingFeature;

    public CrimsonMenuScreen() {
        super(Component.literal("Crimson Client"));
        cfg = CrimsonClientClient.config;

        if (cfg != null) {
            buildFeatures();
        }
    }

    private void add(
            String name,
            String description,
            int category,
            BooleanSupplier getter,
            Consumer<Boolean> setter
    ) {
        features.add(new Feature(
                name, description, category, getter, setter
        ));
    }

    private void buildFeatures() {
        features.clear();

        // COMBAT
        add("KillAura", "Kill Aura", 0,
                () -> cfg.killAura, v -> cfg.killAura = v);
        add("AimAssist", "Aim Assist", 0,
                () -> cfg.aimAssist, v -> cfg.aimAssist = v);
        add("TriggerBot", "Trigger Bot", 0,
                () -> cfg.triggerBot, v -> cfg.triggerBot = v);
        add("ESP", "Entity ESP", 0,
                () -> cfg.esp, v -> cfg.esp = v);
        add("ChestESP", "Chest ESP", 0,
                () -> cfg.chestEsp, v -> cfg.chestEsp = v);
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
        add("TargetESP", "Target ESP", 0,
                () -> cfg.targetEsp, v -> cfg.targetEsp = v);

        // MOVEMENT
        add("FreeCamera", "Free Camera", 1,
                () -> cfg.freeCamera, v -> cfg.freeCamera = v);
        add("Scaffold", "Scaffold", 1,
                () -> cfg.scaffold, v -> cfg.scaffold = v);
        add("Spider", "Spider", 1,
                () -> cfg.spider, v -> cfg.spider = v);
        add("Fly", "Fly", 1,
                () -> cfg.fly, v -> cfg.fly = v);
        add("Trails", "Movement trails", 1,
                () -> cfg.trails, v -> cfg.trails = v);
        add("JumpCircle", "Jump circle", 1,
                () -> cfg.jumpCircle, v -> cfg.jumpCircle = v);
        add("SmoothSword", "Smooth sword", 1,
                () -> cfg.smoothSword, v -> cfg.smoothSword = v);
        add("ItemSwitch", "Item switch animation", 1,
                () -> cfg.itemSwitchAnim, v -> cfg.itemSwitchAnim = v);

        // RENDER
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

        // PLAYER
        add("ChestStealer", "Chest Stealer", 3,
                () -> cfg.chestStealer, v -> cfg.chestStealer = v);
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

        // MISC
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

    private void calculateLayout() {
        panelW = Math.min(width - 24, 1500);
        panelH = Math.min(height - 24, 680);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        columnW = (panelW - 24 - GAP * 4) / 5;
    }

    @Override
    protected void init() {
        calculateLayout();
    }

    private Component styled(String value) {
        return Component.literal(value).setStyle(
                Style.EMPTY.withFont(new FontDescription.Resource(FONT))
        );
    }

    private void text(
            GuiGraphics g, String value,
            int x, int y, int color
    ) {
        g.drawString(font, styled(value), x, y, color, false);
    }

    private void rounded(
            GuiGraphics g,
            int x, int y, int w, int h,
            int radius, int color
    ) {
        if (w <= 0 || h <= 0) return;

        radius = Math.max(1, Math.min(
                radius, Math.min(w / 2, h / 2)
        ));

        for (int i = 0; i < radius; i++) {
            int inset = (int) Math.ceil(
                    radius - Math.sqrt(
                            radius * radius - (radius - i) * (radius - i)
                    )
            );

            g.fill(x + inset, y + i,
                    x + w - inset, y + i + 1, color);
            g.fill(x + inset, y + h - i - 1,
                    x + w - inset, y + h - i, color);
        }

        g.fill(x, y + radius, x + w, y + h - radius, color);
    }

    private boolean inside(
            int mx, int my, int x, int y, int w, int h
    ) {
        return mx >= x && mx <= x + w
                && my >= y && my <= y + h;
    }

    private int columnX(int category) {
        return panelX + 8 + category * (columnW + GAP);
    }

    private List<Feature> categoryFeatures(int category) {
        List<Feature> result = new ArrayList<>();

        for (Feature feature : features) {
            if (feature.category == category) {
                result.add(feature);
            }
        }

        return result;
    }

    private void drawLogo(GuiGraphics g) {
        int size = 27;
        int x = panelX + 13;
        int y = panelY + 10;

        rounded(g, x, y, size, size, 8, ACCENT);
        text(g, "C", x + 8, y + 8, WHITE);

        g.blit(
                RenderPipelines.GUI_TEXTURED,
                LOGO,
                x, y,
                0f, 0f,
                size, size,
                128, 128
        );
    }

    private void drawHeader(GuiGraphics g) {
        drawLogo(g);

        text(g, "CRIMSON", panelX + 49, panelY + 10, WHITE);
        text(g, "CLIENT  /  MODULES",
                panelX + 49, panelY + 25, MUTED);

        int buttonW = 82;
        int buttonH = 25;
        int bx = panelX + panelW - buttonW - 13;
        int by = panelY + 11;

        rounded(g, bx, by, buttonW, buttonH, 7, 0xFF292B35);
        text(g, "CONFIGS", bx + 12, by + 8, WHITE);

        g.fill(panelX + 12, panelY + HEADER_HEIGHT - 1,
                panelX + panelW - 12, panelY + HEADER_HEIGHT,
                0x44FFFFFF);
    }

    private void drawBackground(GuiGraphics g) {
        g.fill(0, 0, width, height, 0x9906080E);

        rounded(g,
                panelX + 3, panelY + 5,
                panelW, panelH, 15, 0x66000000);

        rounded(g,
                panelX, panelY,
                panelW, panelH, 15, 0xF014151D);

        g.fill(panelX + 1, panelY + 1,
                panelX + panelW - 1, panelY + 2,
                ACCENT);
    }

    private void drawColumn(
            GuiGraphics g, int category,
            int mouseX, int mouseY
    ) {
        int x = columnX(category);
        int y = panelY + HEADER_HEIGHT + 7;
        int h = panelH - HEADER_HEIGHT - 17;

        boolean hovered = inside(mouseX, mouseY, x, y, columnW, h);

        rounded(g, x, y, columnW, h, 11,
                hovered ? 0xFF22242E : PANEL);

        text(g, CATEGORIES[category], x + 12, y + 13, WHITE);

        g.fill(x + 10, y + 36,
                x + columnW - 10, y + 37, 0x334F5260);

        List<Feature> list = categoryFeatures(category);

        int startY = y + 45;
        int bottom = y + h - 10;
        int visible = Math.max(1, (bottom - startY) / ROW_HEIGHT);
        int maxScroll = Math.max(0, list.size() - visible);

        scroll[category] = Mth.clamp(
                scroll[category], 0, maxScroll
        );

        int end = Math.min(list.size(), scroll[category] + visible);

        for (int i = scroll[category]; i < end; i++) {
            Feature feature = list.get(i);
            int rowY = startY + (i - scroll[category]) * ROW_HEIGHT;

            drawFeature(g, feature, x + 7, rowY,
                    columnW - 14, mouseX, mouseY);
        }

        if (maxScroll > 0) {
            text(g, (scroll[category] + 1) + "/" + (maxScroll + 1),
                    x + columnW - 36, y + h - 16, MUTED);
        }
    }

    private String keyName(int key) {
        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            return String.valueOf((char) ('A' + key - GLFW.GLFW_KEY_A));
        }

        if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
            return String.valueOf((char) ('0' + key - GLFW.GLFW_KEY_0));
        }

        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F12) {
            return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        }

        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_INSERT -> "INSERT";
            case GLFW.GLFW_KEY_DELETE -> "DELETE";
            default -> "KEY_" + key;
        };
    }

    private int getBind(String name) {
        if (cfg == null || cfg.moduleBinds == null) {
            return -1;
        }

        return cfg.moduleBinds.getOrDefault(name, -1);
    }

    private void setBind(String name, int key) {
        if (cfg == null) return;

        cfg.setBind(name, key);
    }

    private void drawFeature(
            GuiGraphics g,
            Feature feature,
            int x, int y, int w,
            int mouseX, int mouseY
    ) {
        boolean enabled = feature.enabled.getAsBoolean();
        boolean hovered = inside(
                mouseX, mouseY, x, y, w, ROW_HEIGHT - 2
        );

        if (hovered) {
            rounded(g, x, y, w, ROW_HEIGHT - 2, 6, ROW_HOVER);
        }

        text(g, feature.name, x + 8, y + 7,
                enabled ? WHITE : MUTED);

        int bind = getBind(feature.name);
        String bindText = bind < 0 ? "NONE" : keyName(bind);

        text(g, feature.description + " [" + bindText + "]",
                x + 8, y + 21, 0xFF777986);

        int sx = x + w - 28;
        int sy = y + 10;

        rounded(g, sx, sy, 20, 12, 6,
                enabled ? ACCENT : 0xFF444650);

        rounded(g,
                enabled ? sx + 10 : sx + 2,
                sy + 2, 8, 8, 4, WHITE);
    }

    @Override
    public void render(
            GuiGraphics g,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        calculateLayout();

        drawBackground(g);
        drawHeader(g);

        for (int i = 0; i < CATEGORIES.length; i++) {
            drawColumn(g, i, mouseX, mouseY);
        }

        if (waitingForBind && bindingFeature != null) {
            String message = "PRESS A KEY  |  DELETE: CLEAR  |  ESC: CANCEL";
            int w = font.width(styled(message)) + 24;
            int x = (width - w) / 2;
            int y = panelY + panelH - 30;

            rounded(g, x, y, w, 22, 6, 0xFF292B35);
            text(g, message, x + 12, y + 7, ACCENT);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private Feature getFeatureAt(int mx, int my) {
        for (int category = 0; category < 5; category++) {
            int x = columnX(category);
            int y = panelY + HEADER_HEIGHT + 7;
            int startY = y + 45;
            int bottom = y + panelH - HEADER_HEIGHT - 27;

            int visible = Math.max(1,
                    (bottom - startY) / ROW_HEIGHT);

            List<Feature> list = categoryFeatures(category);
            int end = Math.min(list.size(), scroll[category] + visible);

            for (int i = scroll[category]; i < end; i++) {
                int rowY = startY + (i - scroll[category]) * ROW_HEIGHT;

                if (inside(mx, my, x + 7, rowY,
                        columnW - 14, ROW_HEIGHT - 2)) {
                    return list.get(i);
                }
            }
        }

        return null;
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        int mx = (int) event.x();
        int my = (int) event.y();

        if (waitingForBind) {
            return true;
        }

        int bx = panelX + panelW - 95;
        int by = panelY + 11;

        if (event.button() == 0 &&
                inside(mx, my, bx, by, 82, 25)) {
            Minecraft.getInstance().setScreen(new ConfigsScreen(this));
            return true;
        }

        Feature feature = getFeatureAt(mx, my);

        if (feature != null) {
            if (event.button() == 0) {
                feature.setter.accept(!feature.enabled.getAsBoolean());

                if (cfg != null) cfg.save();
                return true;
            }

            if (event.button() == 1) {
                Minecraft.getInstance().setScreen(
                        new ModuleSettingsScreen(this, feature.name)
                );
                return true;
            }

            if (event.button() == 2) {
                waitingForBind = true;
                bindingFeature = feature;
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double deltaX,
            double deltaY
    ) {
        for (int category = 0; category < 5; category++) {
            int x = columnX(category);
            int y = panelY + HEADER_HEIGHT + 7;

            if (inside((int) mouseX, (int) mouseY,
                    x, y, columnW, panelH - HEADER_HEIGHT - 17)) {
                int max = Math.max(0,
                        categoryFeatures(category).size() - 1);

                scroll[category] = Mth.clamp(
                        scroll[category] + (deltaY < 0 ? 1 : -1),
                        0, max
                );

                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
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

            setBind(
                    bindingFeature.name,
                    key == GLFW.GLFW_KEY_DELETE ||
                    key == GLFW.GLFW_KEY_BACKSPACE ? -1 : key
            );

            waitingForBind = false;
            bindingFeature = null;

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
