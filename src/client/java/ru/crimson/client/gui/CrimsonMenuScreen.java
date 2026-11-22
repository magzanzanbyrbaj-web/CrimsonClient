
package ru.crimson.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.EditBox;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CrimsonMenuScreen extends Screen {

    private static final Identifier FONT =
            Identifier.fromNamespaceAndPath("crimsonclient", "inter");

    private static final Identifier LOGO =
            Identifier.fromNamespaceAndPath("crimsonclient", "crimson_logo.png");

    private static final int BG_DARK = 0xF00B0B0E;
    private static final int BG_PANEL = 0xF0111115;
    private static final int BG_CARD = 0xFF151519;
    private static final int BG_CARD_HOVER = 0xFF1B1B21;
    private static final int BG_SIDEBAR = 0xFF0D0D10;
    private static final int ACCENT = 0xFFE63E51;
    private static final int ACCENT_SOFT = 0x33E63E51;
    private static final int TEXT_WHITE = 0xFFF1F1F4;
    private static final int TEXT_MUTED = 0xFF6A6A75;
    private static final int TEXT_DIM = 0xFF484852;
    private static final int BORDER = 0xFF212128;

    private static final String[] CATEGORIES = {
            "Render", "Player", "Display", "Combat", "Configs"
    };

    private static final int CARD_HEIGHT = 50;
    private static final int CARD_GAP = 8;

    private final CrimsonConfig cfg;
    private final List<Feature> features = new ArrayList<>();

    private EditBox searchBox;

    private int selected = 0;
    private int scrollOffset = 0;
    private int settingsScrollOffset = 0;

    private int px, py, pw, ph;
    private int sidebar, cx, cw;

    private Feature settingsFeature;

    private boolean waitingForBind = false;
    private String bindingFeature = null;

    private float animProgress = 1f;
    private long animStart = 0;

    public CrimsonMenuScreen() {
        super(Component.literal("Crimson Client"));
        cfg = CrimsonClientClient.config;

        if (cfg != null) {
            buildFeatures();
        }
    }

    private void buildFeatures() {
        add("HitParticles", "Particles on hit",
                () -> cfg.hitParticles, v -> cfg.hitParticles = v, 0);
        add("CriticalEffects", "Critical hit effects",
                () -> cfg.criticalEffects, v -> cfg.criticalEffects = v, 0);
        add("HitSound", "Custom hit sound",
                () -> cfg.hitSound, v -> cfg.hitSound = v, 0);
        add("Hitmarker", "Hit confirmation",
                () -> cfg.hitmarker, v -> cfg.hitmarker = v, 0);
        add("DamageNumbers", "Show damage numbers",
                () -> cfg.damageNumbers, v -> cfg.damageNumbers = v, 0);
        add("TargetESP", "Highlight current target",
                () -> cfg.targetEsp, v -> cfg.targetEsp = v, 0);
        add("CustomCrosshair", "Custom crosshair",
                () -> cfg.customCrosshair, v -> cfg.customCrosshair = v, 0);
        add("CustomSky", "Custom sky",
                () -> cfg.customSky, v -> cfg.customSky = v, 0);
        add("ChinaHat", "China hat effect",
                () -> cfg.chinaHat, v -> cfg.chinaHat = v, 0);
        add("Trails", "Movement trails",
                () -> cfg.trails, v -> cfg.trails = v, 0);
        add("ChatAnimations", "Smooth chat",
                () -> cfg.chatAnimations, v -> cfg.chatAnimations = v, 0);
        add("JumpCircle", "Jump circle",
                () -> cfg.jumpCircle, v -> cfg.jumpCircle = v, 0);
        add("CustomHitBox", "Custom hitboxes",
                () -> cfg.customHitbox, v -> cfg.customHitbox = v, 0);
        add("BlockOverlay", "Block outline",
                () -> cfg.blockOverlay, v -> cfg.blockOverlay = v, 0);
        add("NoRender", "Disable some effects",
                () -> cfg.noRender, v -> cfg.noRender = v, 0);
        add("InventoryBlur", "Inventory blur",
                () -> cfg.inventoryBlur, v -> cfg.inventoryBlur = v, 0);
        add("SmoothSword", "Smooth sword swing",
                () -> cfg.smoothSword, v -> cfg.smoothSword = v, 0);
        add("ItemSwitch", "Item switch animation",
                () -> cfg.itemSwitchAnim, v -> cfg.itemSwitchAnim = v, 0);
        add("TotemAnimation", "Totem animation",
                () -> cfg.totemAnimation, v -> cfg.totemAnimation = v, 0);
        add("ReachDisplay", "Show reach distance",
                () -> cfg.reachDisplay, v -> cfg.reachDisplay = v, 0);
        add("PearlTracker", "Pearl tracker",
                () -> cfg.pearlTracker, v -> cfg.pearlTracker = v, 0);

        add("TargetHUD", "Target information",
                () -> cfg.targetHud, v -> cfg.targetHud = v, 1);
        add("ArmorHUD", "Armor display",
                () -> cfg.armorHud, v -> cfg.armorHud = v, 1);
        add("TotemCounter", "Totem counter",
                () -> cfg.totemCounter, v -> cfg.totemCounter = v, 1);
        add("InventoryHUD", "Inventory HUD",
                () -> cfg.inventoryHud, v -> cfg.inventoryHud = v, 1);
        add("PotionHUD", "Potion effects",
                () -> cfg.potionHud, v -> cfg.potionHud = v, 1);
        add("DurabilityHUD", "Item durability",
                () -> cfg.durabilityHud, v -> cfg.durabilityHud = v, 1);

        add("Watermark", "Client watermark",
                () -> cfg.watermark, v -> cfg.watermark = v, 2);
        add("Keystrokes", "Keystrokes display",
                () -> cfg.keystrokes, v -> cfg.keystrokes = v, 2);
        add("ModuleList", "Enabled modules list",
                () -> cfg.moduleList, v -> cfg.moduleList = v, 2);

        add("KillAura", "Auto attack targets",
                () -> cfg.killAura, v -> cfg.killAura = v, 3);
        add("TriggerBot", "Attack on crosshair",
                () -> cfg.triggerBot, v -> cfg.triggerBot = v, 3);
        add("AimAssist", "Smooth aim assistance",
                () -> cfg.aimAssist, v -> cfg.aimAssist = v, 3);
        add("ESP", "Entity ESP",
                () -> cfg.esp, v -> cfg.esp = v, 3);

        add("Fullbright", "Fullbright",
                () -> cfg.fullbright, v -> cfg.fullbright = v, 4);
        add("ReduceParticles", "Reduce particles",
                () -> cfg.reduceParticles, v -> cfg.reduceParticles = v, 4);
        add("FPSBoost", "Performance boost",
                () -> cfg.fpsBoost, v -> cfg.fpsBoost = v, 4);
        add("Optimization", "Optimizations",
                () -> cfg.optimized, v -> cfg.optimized = v, 4);
        add("MenuAnimation", "Menu animation",
                () -> cfg.glassAnimation, v -> cfg.glassAnimation = v, 4);
    }

    private void add(
            String name,
            String desc,
            BooleanSupplier get,
            Consumer<Boolean> set,
            int category
    ) {
        features.add(new Feature(name, desc, get, set, category));
    }

    private void layout() {
        pw = Math.min(width - 36, 740);
        ph = Math.min(height - 36, 440);

        px = (width - pw) / 2;
        py = (height - ph) / 2;

        sidebar = 148;
        cx = px + sidebar + 18;
        cw = pw - sidebar - 36;
    }

    private void startAnim(int newSelected) {
        if (newSelected == selected) return;

        selected = newSelected;
        animStart = System.currentTimeMillis();
        animProgress = 0f;

        scrollOffset = 0;
        settingsScrollOffset = 0;
        settingsFeature = null;
        waitingForBind = false;
        bindingFeature = null;

        if (searchBox != null) {
            searchBox.setValue("");
        }
    }

    private float getAnim() {
        if (animProgress >= 1f) return 1f;

        long elapsed = System.currentTimeMillis() - animStart;
        animProgress = Mth.clamp(elapsed / 180f, 0f, 1f);

        return 1f - (1f - animProgress) * (1f - animProgress);
    }

    @Override
    protected void init() {
        layout();

        int searchW = 170;

        searchBox = new EditBox(
                font,
                px + pw - searchW - 18,
                py + 15,
                searchW,
                22,
                Component.literal("Search")
        );

        searchBox.setHint(Component.literal("Search..."));
        searchBox.setMaxLength(64);
        searchBox.setBordered(false);
        searchBox.setTextColor(0xFFC8C8D0);

        addRenderableWidget(searchBox);
    }

    private void text(
            GuiGraphics g,
            String value,
            int x,
            int y,
            int color
    ) {
        Component component = Component.literal(value)
                .setStyle(Style.EMPTY.withFont(
                        new FontDescription.Resource(FONT)
                ));

        g.drawString(font, component, x, y, color, false);
    }

    private void rounded(
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

            g.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
            g.fill(x + inset, y + h - row - 1,
                    x + w - inset, y + h - row, color);
        }

        g.fill(x, y + r, x + w, y + h - r, color);
    }

    private boolean hit(
            int mx,
            int my,
            int x,
            int y,
            int w,
            int h
    ) {
        return mx >= x && mx <= x + w
                && my >= y && my <= y + h;
    }

    private List<Feature> filteredFeatures() {
        List<Feature> result = new ArrayList<>();

        String query = searchBox == null
                ? ""
                : searchBox.getValue().trim().toLowerCase(Locale.ROOT);

        for (Feature feature : features) {
            if (query.isEmpty()) {
                if (feature.category == selected) {
                    result.add(feature);
                }
            } else if (
                    feature.name.toLowerCase(Locale.ROOT).contains(query)
                            || feature.desc.toLowerCase(Locale.ROOT).contains(query)
            ) {
                result.add(feature);
            }
        }

        return result;
    }

    private int cardAreaWidth() {
        return settingsFeature == null ? cw : Math.max(130, cw - 196);
    }

    private int settingsPanelX() {
        return px + pw - 186 - 14;
    }

    @Override
    public void render(
            GuiGraphics g,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        layout();

        float anim = getAnim();

        g.fill(0, 0, width, height, 0xA0000000);

        rounded(g, px + 3, py + 4, pw, ph, 14, 0x55000000);
        rounded(g, px, py, pw, ph, 14, BG_DARK);
        rounded(g, px + 1, py + 1, pw - 2, ph - 2, 13, BG_PANEL);

        rounded(g, px + 1, py + 1, sidebar, ph - 2, 13, BG_SIDEBAR);

        g.fill(
                px + sidebar,
                py + 14,
                px + sidebar + 1,
                py + ph - 14,
                BORDER
        );

        drawSidebar(g, mouseX, mouseY);
        drawHeader(g);

        int contentOffset = (int) ((1f - anim) * 18f);

        g.pose().pushMatrix();
        g.pose().translate(contentOffset, 0);

        drawCards(g, mouseX, mouseY);
        drawSettings(g);

        g.pose().popMatrix();

        if (waitingForBind) {
            String message = "PRESS A KEY  |  DELETE - UNBIND  |  ESC - CANCEL";
            int w = font.width(message) + 24;
            int x = (width - w) / 2;
            int y = height - 34;

            rounded(g, x, y, w, 24, 8, 0xFF21171B);
            text(g, message, x + 12, y + 8, ACCENT);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawSidebar(GuiGraphics g, int mx, int my) {
        g.blit(
                RenderPipelines.GUI_TEXTURED,
                LOGO,
                px + 18,
                py + 18,
                0,
                0,
                26,
                26,
                256,
                256
        );

        text(g, "CRIMSON", px + 50, py + 20, ACCENT);
        text(g, "CLIENT", px + 50, py + 32, TEXT_MUTED);

        for (int i = 0; i < CATEGORIES.length; i++) {
            int bx = px + 14;
            int by = py + 72 + i * 38;

            boolean active = selected == i;
            boolean hover = hit(mx, my, bx, by, sidebar - 28, 32);

            if (active) {
                rounded(g, bx, by, sidebar - 28, 32, 9, 0xFF1A1A21);
                rounded(g, bx, by + 8, 3, 16, 2, ACCENT);
            } else if (hover) {
                rounded(g, bx, by, sidebar - 28, 32, 9, 0xFF15151B);
            }

            text(
                    g,
                    CATEGORIES[i],
                    bx + 16,
                    by + 11,
                    active ? TEXT_WHITE : TEXT_MUTED
            );
        }

        int profileY = py + ph - 56;

        rounded(g, px + 14, profileY, sidebar - 28, 38, 9, 0xFF131317);
        rounded(g, px + 20, profileY + 8, 22, 22, 6, 0xFF28282F);

        String name = Minecraft.getInstance().player != null
                ? Minecraft.getInstance().player.getName().getString()
                : "Player";

        if (name.length() > 11) {
            name = name.substring(0, 11);
        }

        text(g, name, px + 48, profileY + 10, TEXT_WHITE);
        text(g, "Online", px + 48, profileY + 22, TEXT_DIM);
    }

    private void drawHeader(GuiGraphics g) {
        int searchW = 170;
        int titleW = cw - searchW - 14;

        rounded(g, cx, py + 15, titleW, 28, 9, BG_CARD);

        text(g, CATEGORIES[selected], cx + 14, py + 23, TEXT_WHITE);

        int sx = px + pw - searchW - 16;

        rounded(g, sx, py + 15, searchW, 28, 9, BG_CARD);
        text(g, "⌕", sx + searchW - 24, py + 22, TEXT_MUTED);
    }

    private void drawCards(GuiGraphics g, int mx, int my) {
        List<Feature> visible = filteredFeatures();

        int areaW = cardAreaWidth();
        int columns = settingsFeature == null ? 2 : 1;
        int gap = 10;

        int cardW = (areaW - gap * (columns - 1)) / columns;

        int startY = py + 56;
        int bottom = py + ph - 18;

        int rowsVisible = Math.max(
                1,
                (bottom - startY + CARD_GAP) / (CARD_HEIGHT + CARD_GAP)
        );

        int maxOffset = Math.max(
                0,
                (int) Math.ceil((double) visible.size() / columns) - rowsVisible
        );

        scrollOffset = Mth.clamp(scrollOffset, 0, maxOffset);

        int startIndex = scrollOffset * columns;
        int endIndex = Math.min(
                visible.size(),
                startIndex + rowsVisible * columns
        );

        if (visible.isEmpty()) {
            text(g, "Nothing found", cx + 8, startY + 22, TEXT_MUTED);
            return;
        }

        for (int i = startIndex; i < endIndex; i++) {
            Feature feature = visible.get(i);

            int local = i - startIndex;
            int col = local % columns;
            int row = local / columns;

            int x = cx + col * (cardW + gap);
            int y = startY + row * (CARD_HEIGHT + CARD_GAP);

            boolean enabled = feature.get.getAsBoolean();
            boolean hover = hit(mx, my, x, y, cardW, CARD_HEIGHT);
            boolean chosen = settingsFeature == feature;

            rounded(
                    g,
                    x,
                    y,
                    cardW,
                    CARD_HEIGHT,
                    11,
                    hover || chosen ? BG_CARD_HOVER : BG_CARD
            );

            if (enabled || chosen) {
                g.fill(
                        x + 1,
                        y,
                        x + cardW - 1,
                        y + 2,
                        chosen ? ACCENT : 0xFF35353E
                );
            }

            text(
                    g,
                    feature.name,
                    x + 14,
                    y + 13,
                    enabled ? TEXT_WHITE : TEXT_MUTED
            );

            text(g, feature.desc, x + 14, y + 29, TEXT_DIM);

            int indX = x + cardW - 30;
            int indY = y + 17;

            rounded(
                    g,
                    indX,
                    indY,
                    18,
                    18,
                    6,
                    enabled ? ACCENT_SOFT : 0xFF1C1C22
            );

            if (enabled) {
                rounded(g, indX + 4, indY + 4, 10, 10, 4, ACCENT);
            }

            if (chosen) {
                text(g, "⚙", x + cardW - 52, y + 18, ACCENT);
            }
        }
    }

    private record SettingRow(
            String name,
            BooleanSupplier enabled,
            Runnable toggle,
            boolean bindRow
    ) {}

    private SettingRow toggleRow(
            String name,
            BooleanSupplier enabled,
            Runnable toggle
    ) {
        return new SettingRow(name, enabled, toggle, false);
    }

    private SettingRow bindRow() {
        String name = waitingForBind && settingsFeature != null
                && settingsFeature.name.equals(bindingFeature)
                ? "Bind: Press a key..."
                : "Bind: " + keyName(getBind(settingsFeature.name));

        return new SettingRow(
                name,
                () -> getBind(settingsFeature.name) != -1,
                () -> {
                    waitingForBind = true;
                    bindingFeature = settingsFeature.name;
                },
                true
        );
    }

    private List<SettingRow> getSettingsRows() {
        if (settingsFeature == null) {
            return Collections.emptyList();
        }

        List<SettingRow> rows = new ArrayList<>();

        switch (settingsFeature.name) {
            case "KillAura" -> {
                rows.add(toggleRow("Silent rotation",
                        () -> cfg.kaSilent,
                        () -> cfg.kaSilent = !cfg.kaSilent));

                rows.add(toggleRow("Through walls",
                        () -> cfg.kaThroughWalls,
                        () -> cfg.kaThroughWalls = !cfg.kaThroughWalls));

                rows.add(toggleRow("Only Criticals",
                        () -> cfg.kaOnlyCriticals,
                        () -> cfg.kaOnlyCriticals = !cfg.kaOnlyCriticals));

                rows.add(toggleRow(
                        "Range: " + String.format(Locale.ROOT, "%.1f", cfg.kaRange),
                        () -> true,
                        () -> {
                            cfg.kaRange += 0.5;
                            if (cfg.kaRange > 6.0) cfg.kaRange = 1.0;
                        }
                ));

                rows.add(toggleRow(
                        "Priority: " + priorityName(cfg.kaPriority),
                        () -> true,
                        () -> cfg.kaPriority = (cfg.kaPriority + 1) % 3
                ));
            }

            case "TriggerBot" -> {
                rows.add(toggleRow("Through walls",
                        () -> cfg.tbThroughWalls,
                        () -> cfg.tbThroughWalls = !cfg.tbThroughWalls));

                rows.add(toggleRow("Only Criticals",
                        () -> cfg.tbOnlyCriticals,
                        () -> cfg.tbOnlyCriticals = !cfg.tbOnlyCriticals));

                rows.add(toggleRow(
                        "Range: " + String.format(Locale.ROOT, "%.1f", cfg.tbRange),
                        () -> true,
                        () -> {
                            cfg.tbRange += 0.5;
                            if (cfg.tbRange > 6.0) cfg.tbRange = 1.0;
                        }
                ));
            }

            case "AimAssist" -> {
                rows.add(toggleRow("Silent rotation",
                        () -> cfg.aaSilent,
                        () -> cfg.aaSilent = !cfg.aaSilent));

                rows.add(toggleRow("Through walls",
                        () -> cfg.aaThroughWalls,
                        () -> cfg.aaThroughWalls = !cfg.aaThroughWalls));

                rows.add(toggleRow(
                        "Range: " + String.format(Locale.ROOT, "%.1f", cfg.aaRange),
                        () -> true,
                        () -> {
                            cfg.aaRange += 0.5;
                            if (cfg.aaRange > 6.0) cfg.aaRange = 1.0;
                        }
                ));
            }

            case "ESP" -> {
                rows.add(toggleRow("Players",
                        () -> cfg.espPlayers,
                        () -> cfg.espPlayers = !cfg.espPlayers));

                rows.add(toggleRow("Mobs",
                        () -> cfg.espMobs,
                        () -> cfg.espMobs = !cfg.espMobs));
            }

            case "HitSound" -> rows.add(toggleRow(
                    "Sound: " + hitSoundName(),
                    () -> true,
                    () -> cfg.hitSoundType = (cfg.hitSoundType + 1) % 7
            ));

            case "CriticalEffects" -> rows.add(toggleRow(
                    "Style: " + criticalName(),
                    () -> true,
                    () -> cfg.criticalStyle = (cfg.criticalStyle + 1) % 4
            ));

            case "ChinaHat" -> rows.add(toggleRow(
                    "Rainbow",
                    () -> cfg.chinaHatRainbow,
                    () -> cfg.chinaHatRainbow = !cfg.chinaHatRainbow
            ));

            case "Trails" -> rows.add(toggleRow(
                    "Length: " + cfg.trailsLength,
                    () -> true,
                    () -> {
                        cfg.trailsLength += 10;
                        if (cfg.trailsLength > 100) cfg.trailsLength = 20;
                    }
            ));

            case "TargetESP" -> rows.add(toggleRow(
                    "Ring",
                    () -> cfg.targetEspRing,
                    () -> cfg.targetEspRing = !cfg.targetEspRing
            ));

            case "NoRender" -> rows.add(toggleRow(
                    "Hide fire",
                    () -> cfg.hideFire,
                    () -> cfg.hideFire = !cfg.hideFire
            ));
        }

        rows.add(bindRow());
        return rows;
    }

    private int getBind(String feature) {
        if (feature == null) return -1;

        return switch (feature) {
            case "KillAura" -> cfg.bindKillAura;
            case "TriggerBot" -> cfg.bindTriggerBot;
            case "AimAssist" -> cfg.bindAimAssist;
            case "ESP" -> cfg.bindESP;
            case "TargetHUD" -> cfg.bindTargetHUD;
            case "ArmorHUD" -> cfg.bindArmorHUD;
            case "TotemCounter" -> cfg.bindTotemCounter;
            case "Watermark" -> cfg.bindWatermark;
            case "Keystrokes" -> cfg.bindKeystrokes;
            case "ModuleList" -> cfg.bindModuleList;
            case "InventoryHUD" -> cfg.bindInventoryHUD;
            case "PotionHUD" -> cfg.bindPotionHUD;
            case "DurabilityHUD" -> cfg.bindDurabilityHUD;
            default -> -1;
        };
    }

    private void setBind(String feature, int key) {
        if (feature == null) return;

        switch (feature) {
            case "KillAura" -> cfg.bindKillAura = key;
            case "TriggerBot" -> cfg.bindTriggerBot = key;
            case "AimAssist" -> cfg.bindAimAssist = key;
            case "ESP" -> cfg.bindESP = key;
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

    private String keyName(int key) {
        if (key < 0) return "NONE";

        String name = GLFW.glfwGetKeyName(key, 0);

        if (name != null) {
            return name.toUpperCase(Locale.ROOT);
        }

        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "L-SHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "R-SHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "L-CTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "R-CTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "L-ALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "R-ALT";
            case GLFW.GLFW_KEY_F1 -> "F1";
            case GLFW.GLFW_KEY_F2 -> "F2";
            case GLFW.GLFW_KEY_F3 -> "F3";
            case GLFW.GLFW_KEY_F4 -> "F4";
            case GLFW.GLFW_KEY_F5 -> "F5";
            case GLFW.GLFW_KEY_F6 -> "F6";
            case GLFW.GLFW_KEY_F7 -> "F7";
            case GLFW.GLFW_KEY_F8 -> "F8";
            case GLFW.GLFW_KEY_F9 -> "F9";
            case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11";
            case GLFW.GLFW_KEY_F12 -> "F12";
            default -> "KEY " + key;
        };
    }

    private void drawSettings(GuiGraphics g) {
        if (settingsFeature == null) return;

        int panelW = 182;
        int panelH = ph - 74;
        int x = settingsPanelX();
        int y = py + 56;

        rounded(g, x, y, panelW, panelH, 11, BG_CARD);

        text(g, "SETTINGS", x + 14, y + 15, ACCENT);
        text(g, settingsFeature.name, x + 14, y + 34, TEXT_WHITE);

        g.fill(x + 14, y + 51, x + panelW - 14, y + 52, BORDER);

        List<SettingRow> rows = getSettingsRows();

        int rowY = y + 63;
        int rowHeight = 34;

        int visibleRows = Math.max(1, (panelH - 125) / rowHeight);
        int maxScroll = Math.max(0, rows.size() - visibleRows);

        settingsScrollOffset = Mth.clamp(
                settingsScrollOffset,
                0,
                maxScroll
        );

        int end = Math.min(
                rows.size(),
                settingsScrollOffset + visibleRows
        );

        for (int i = settingsScrollOffset; i < end; i++) {
            SettingRow row = rows.get(i);

            boolean enabled = row.enabled.getAsBoolean();

            rounded(
                    g,
                    x + 10,
                    rowY,
                    panelW - 20,
                    29,
                    7,
                    row.bindRow
                            ? 0xFF24191D
                            : enabled ? 0xFF21171B : 0xFF19191F
            );

            text(
                    g,
                    row.name,
                    x + 18,
                    rowY + 10,
                    row.bindRow ? ACCENT : enabled ? TEXT_WHITE : TEXT_MUTED
            );

            if (!row.bindRow) {
                rounded(
                        g,
                        x + panelW - 39,
                        rowY + 8,
                        19,
                        13,
                        6,
                        enabled ? ACCENT : 0xFF303038
                );

                int dotX = enabled
                        ? x + panelW - 29
                        : x + panelW - 36;

                rounded(g, dotX, rowY + 10, 9, 9, 4, TEXT_WHITE);
            }

            rowY += rowHeight;
        }

        rounded(
                g,
                x + 10,
                y + panelH - 42,
                panelW - 20,
                30,
                8,
                0xFF18181E
        );

        text(g, "LMB - Toggle", x + 20, y + panelH - 32, TEXT_MUTED);
    }

    private String priorityName(int priority) {
        return switch (priority) {
            case 1 -> "Health";
            case 2 -> "Armor";
            default -> "Closest";
        };
    }

    private String hitSoundName() {
        return switch (Math.floorMod(cfg.hitSoundType, 7)) {
            case 1 -> "Critical";
            case 2 -> "Strong";
            case 3 -> "Knockback";
            case 4 -> "Arrow";
            case 5 -> "XP";
            case 6 -> "Pling";
            default -> "Weak";
        };
    }

    private String criticalName() {
        return switch (Math.floorMod(cfg.criticalStyle, 4)) {
            case 1 -> "Ghosts";
            case 2 -> "Spiral";
            case 3 -> "Burst";
            default -> "Staircase";
        };
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        int mx = (int) event.x();
        int my = (int) event.y();
        int button = event.button();

        if (waitingForBind) {
            return true;
        }

        if (searchBox != null
                && searchBox.isMouseOver(event.x(), event.y())) {
            return super.mouseClicked(event, doubleClick);
        }

        if (button == 0) {
            for (int i = 0; i < CATEGORIES.length; i++) {
                int bx = px + 14;
                int by = py + 72 + i * 38;

                if (hit(mx, my, bx, by, sidebar - 28, 32)) {
                    if (i == 4) {
                        Minecraft.getInstance().setScreen(
                                new ConfigsScreen(this)
                        );
                        return true;
                    }

                    startAnim(i);
                    return true;
                }
            }
        }

        if (settingsFeature != null) {
            int x = settingsPanelX();
            int y = py + 56;
            int panelH = ph - 74;

            if (hit(mx, my, x, y, 182, panelH)) {
                if (button == 1) {
                    settingsFeature = null;
                    settingsScrollOffset = 0;
                    return true;
                }

                if (button == 0) {
                    List<SettingRow> rows = getSettingsRows();

                    int rowY = y + 63;
                    int rowHeight = 34;

                    int visibleRows = Math.max(
                            1,
                            (panelH - 125) / rowHeight
                    );

                    int end = Math.min(
                            rows.size(),
                            settingsScrollOffset + visibleRows
                    );

                    for (int i = settingsScrollOffset; i < end; i++) {
                        if (hit(mx, my, x + 10, rowY, 162, 29)) {
                            SettingRow row = rows.get(i);

                            if (row.bindRow) {
                                waitingForBind = true;
                                bindingFeature = settingsFeature.name;
                            } else {
                                row.toggle.run();
                                cfg.save();
                            }

                            return true;
                        }

                        rowY += rowHeight;
                    }

                    return true;
                }
            }
        }

        List<Feature> visible = filteredFeatures();

        int areaW = cardAreaWidth();
        int columns = settingsFeature == null ? 2 : 1;
        int gap = 10;

        int cardW = (areaW - gap * (columns - 1)) / columns;

        int startY = py + 56;
        int bottom = py + ph - 18;

        int rowsVisible = Math.max(
                1,
                (bottom - startY + CARD_GAP) / (CARD_HEIGHT + CARD_GAP)
        );

        int startIndex = scrollOffset * columns;

        int endIndex = Math.min(
                visible.size(),
                startIndex + rowsVisible * columns
        );

        for (int i = startIndex; i < endIndex; i++) {
            int local = i - startIndex;

            int col = local % columns;
            int row = local / columns;

            int x = cx + col * (cardW + gap);
            int y = startY + row * (CARD_HEIGHT + CARD_GAP);

            if (!hit(mx, my, x, y, cardW, CARD_HEIGHT)) {
                continue;
            }

            Feature feature = visible.get(i);

            if (button == 1) {
                settingsFeature = settingsFeature == feature
                        ? null
                        : feature;

                settingsScrollOffset = 0;
                return true;
            }

            if (button == 0) {
                feature.set.accept(!feature.get.getAsBoolean());
                cfg.save();
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        if (waitingForBind) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                waitingForBind = false;
                bindingFeature = null;
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_DELETE
                    || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                setBind(bindingFeature, -1);
            } else {
                setBind(bindingFeature, keyCode);
            }

            waitingForBind = false;
            bindingFeature = null;
            cfg.save();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double deltaX,
            double deltaY
    ) {
        if (settingsFeature != null
                && hit(
                (int) mouseX,
                (int) mouseY,
                settingsPanelX(),
                py + 56,
                182,
                ph - 74
        )) {
            int visibleRows = Math.max(1, (ph - 74 - 125) / 34);

            int maxScroll = Math.max(
                    0,
                    getSettingsRows().size() - visibleRows
            );

            settingsScrollOffset = Mth.clamp(
                    settingsScrollOffset + (deltaY < 0 ? 1 : -1),
                    0,
                    maxScroll
            );

            return true;
        }

        if (hit(
                (int) mouseX,
                (int) mouseY,
                cx,
                py + 50,
                cardAreaWidth(),
                ph - 60
        )) {
            scrollOffset = Math.max(
                    0,
                    scrollOffset + (deltaY < 0 ? 1 : -1)
            );

            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public void onClose() {
        if (cfg != null) cfg.save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record Feature(
            String name,
            String desc,
            BooleanSupplier get,
            Consumer<Boolean> set,
            int category
    ) {}
}
