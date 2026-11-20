package ru.crimson.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
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

    private static final int RED = 0xFFFF4055;
    private static final int WHITE = 0xFFF5F3F7;
    private static final int MUTED = 0xFF9295A5;

    private static final String[] CATEGORIES = {
            "Combat", "Movement", "Player",
            "Visuals", "Client", "Configs"
    };

    private final CrimsonConfig cfg;
    private final long openedAt = System.currentTimeMillis();
    private final List<Feature> features = new ArrayList<>();

    private EditBox searchBox;
    private int selected = 3;
    private float anim = 1f;

    private int px, py, pw, ph;
    private int sidebar, cx, cw;

    // Side settings panel (opens on RMB)
    private Feature settingsFeature;
    private int settingsScroll;

    public CrimsonMenuScreen() {
        super(Component.literal("Crimson Client"));
        cfg = CrimsonClientClient.config;
        buildFeatures();
    }

    private void buildFeatures() {
        // Combat
        add("Target HUD", "Target health and distance",
                () -> cfg.targetHud, v -> cfg.targetHud = v, 0);
        add("Hit Particles", "Visual hit particles",
                () -> cfg.hitParticles, v -> cfg.hitParticles = v, 0);
        add("Damage Numbers", "Damage number visuals",
                () -> cfg.damageNumbers, v -> cfg.damageNumbers = v, 0);
        add("Hitmarker", "Hit confirmation indicator",
                () -> cfg.hitmarker, v -> cfg.hitmarker = v, 0);
        add("Critical Effects", "Critical hit visuals",
                () -> cfg.criticalEffects, v -> cfg.criticalEffects = v, 0);
        add("Target ESP", "Particles & ring on hit target",
                () -> cfg.targetEsp, v -> cfg.targetEsp = v, 0);
        add("Hit Sound", "Custom sound on hit",
                () -> cfg.hitSound, v -> cfg.hitSound = v, 0);

        // Player
        add("Armor HUD", "Equipment display",
                () -> cfg.armorHud, v -> cfg.armorHud = v, 2);
        add("Totem Counter", "Inventory totem count",
                () -> cfg.totemCounter, v -> cfg.totemCounter = v, 2);
        add("Inventory HUD", "Inventory item display",
                () -> cfg.inventoryHud, v -> cfg.inventoryHud = v, 2);
        add("Potion HUD", "Active potion effects",
                () -> cfg.potionHud, v -> cfg.potionHud = v, 2);
        add("Durability HUD", "Item durability",
                () -> cfg.durabilityHud, v -> cfg.durabilityHud = v, 2);

        // Visuals
        add("Smooth Sword", "Sword animation",
                () -> cfg.smoothSword, v -> cfg.smoothSword = v, 3);
        add("Item Switch", "Item switch animation",
                () -> cfg.itemSwitchAnim, v -> cfg.itemSwitchAnim = v, 3);
        add("Totem Animation", "Totem animation",
                () -> cfg.totemAnimation, v -> cfg.totemAnimation = v, 3);
        add("Custom Crosshair", "Custom crosshair (hides vanilla)",
                () -> cfg.customCrosshair, v -> cfg.customCrosshair = v, 3);
        add("Custom Sky", "Sky color / style",
                () -> cfg.customSky, v -> cfg.customSky = v, 3);
        add("Inventory Blur", "Inventory background effect",
                () -> cfg.inventoryBlur, v -> cfg.inventoryBlur = v, 3);
        add("Jump Circle", "Circle under feet on jump",
                () -> cfg.jumpCircle, v -> cfg.jumpCircle = v, 3);
        add("Custom Hitbox", "Player hitboxes (no invisible)",
                () -> cfg.customHitbox, v -> cfg.customHitbox = v, 3);
        add("Block Overlay", "RGB / gradient block outline",
                () -> cfg.blockOverlay, v -> cfg.blockOverlay = v, 3);
        add("No Render", "Hide fire, portal, weather...",
                () -> cfg.noRender, v -> cfg.noRender = v, 3);

        // Client
        add("Watermark", "Logo, FPS and ping",
                () -> cfg.watermark, v -> cfg.watermark = v, 4);
        add("Keystrokes", "Keyboard display",
                () -> cfg.keystrokes, v -> cfg.keystrokes = v, 4);
        add("Module List", "Enabled module list",
                () -> cfg.moduleList, v -> cfg.moduleList = v, 4);
        add("Fullbright", "World brightness",
                () -> cfg.fullbright, v -> cfg.fullbright = v, 4);
        add("Reduce Particles", "Reduce particle rendering",
                () -> cfg.reduceParticles, v -> cfg.reduceParticles = v, 4);
        add("Optimization", "Client optimization",
                () -> cfg.optimized, v -> cfg.optimized = v, 4);
        add("Menu Animation", "Menu animations",
                () -> cfg.glassAnimation, v -> cfg.glassAnimation = v, 4);
    }

    private void add(String name, String desc,
                     BooleanSupplier get, Consumer<Boolean> set,
                     int category) {
        features.add(new Feature(name, desc, get, set, category));
    }

    private void layout() {
        pw = Math.min(560, width - 20);
        ph = Math.min(360, height - 20);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        sidebar = 124;
        cx = px + sidebar + 18;
        cw = pw - sidebar - 34;
    }

    @Override
    protected void init() {
        layout();

        searchBox = new EditBox(
                font,
                cx,
                py + 48,
                cw,
                20,
                Component.literal("Search")
        );

        searchBox.setHint(Component.literal("Search functions..."));
        searchBox.setMaxLength(64);
        addRenderableWidget(searchBox);
    }

    private void text(GuiGraphics g, String value,
                      int x, int y, int color, boolean shadow) {
        Component component = Component.literal(value)
                .setStyle(Style.EMPTY.withFont(
                        new FontDescription.Resource(FONT)
                ));
        g.drawString(font, component, x, y, color, shadow);
    }

    private List<Feature> filteredFeatures() {
        List<Feature> result = new ArrayList<>();
        String query = searchBox == null ? "" :
                searchBox.getValue().trim().toLowerCase(Locale.ROOT);

        for (Feature f : features) {
            if (query.isEmpty()) {
                if (f.category == selected) result.add(f);
            } else {
                String name = f.name.toLowerCase(Locale.ROOT);
                String desc = f.desc.toLowerCase(Locale.ROOT);

                if (name.contains(query) || desc.contains(query)) {
                    result.add(f);
                }
            }
        }
        return result;
    }

    @Override
    public void render(GuiGraphics g, int mouseX,
                       int mouseY, float partialTick) {
        layout();

        float t = Mth.clamp(
                (System.currentTimeMillis() - openedAt) / 260f,
                0f, 1f
        );

        anim = cfg.glassAnimation
                ? 1f - (float) Math.pow(1f - t, 3)
                : 1f;

        g.fill(0, 0, width, height, 0x88030812);

        int lift = (int) ((1f - anim) * 9);
        int y = py + lift;

        rounded(g, px + 3, y + 5, pw, ph, 16, 0x66000000);
        rounded(g, px, y, pw, ph, 16, alpha(0xED111521));
        rounded(g, px + 1, y + 1, pw - 2, ph - 2,
                15, alpha(0xD91B2030));
        rounded(g, px + 14, y + 1, pw - 28, 2,
                1, alpha(RED));
        rounded(g, px + 1, y + 1, sidebar, ph - 2,
                14, alpha(0xB80B0F19));

        g.fill(px + sidebar, y + 16,
                px + sidebar + 1, y + ph - 16,
                alpha(0x33FFFFFF));

        drawSidebar(g, mouseX, mouseY, y);
        drawHeader(g, y);
        drawCards(g, mouseX, mouseY, y);

        if (settingsFeature != null) {
            drawSettingsPanel(g, mouseX, mouseY, y);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawSidebar(GuiGraphics g, int mx, int my, int y) {
        rounded(g, px + 13, y + 14, 27, 27, 8,
                alpha(0xFF55202F));
        text(g, "C", px + 22, y + 23, alpha(0xFFFF6374), true);
        text(g, "CRIMSON", px + 46, y + 17, alpha(WHITE), true);
        text(g, "CLIENT", px + 46, y + 29, alpha(MUTED), false);

        for (int i = 0; i < CATEGORIES.length; i++) {
            int bx = px + 8;
            int by = y + 57 + i * 34;
            int bw = sidebar - 16;

            boolean active = selected == i;
            boolean hover = hit(mx, my, bx, by, bw, 28);

            if (active) {
                rounded(g, bx, by, bw, 28, 7,
                        alpha(0xAA482331));
                rounded(g, bx, by + 4, 2, 20, 1, alpha(RED));
            } else if (hover) {
                rounded(g, bx, by, bw, 28, 7,
                        alpha(0x553B4052));
            }

            text(g, CATEGORIES[i], bx + 12, by + 10,
                    alpha(active ? WHITE : MUTED), false);

            int count = 0;
            for (Feature f : features) {
                if (f.category == i) count++;
            }

            String n = String.valueOf(count);
            text(g, n, bx + bw - widthOf(n) - 9,
                    by + 10, alpha(MUTED), false);
        }

        int profileY = y + ph - 43;
        rounded(g, px + 8, profileY, sidebar - 16, 33,
                7, alpha(0x553C4050));
        rounded(g, px + 13, profileY + 5, 22, 22,
                6, alpha(0xFF542432));
        text(g, "C", px + 21, profileY + 12,
                alpha(WHITE), true);

        String player = Minecraft.getInstance().player == null
                ? "Player"
                : Minecraft.getInstance().player.getName().getString();

        text(g, player.length() > 12 ? player.substring(0, 12) : player,
                px + 41, profileY + 7, alpha(WHITE), false);
        text(g, "Crimson user", px + 41, profileY + 19,
                alpha(MUTED), false);
    }

    private void drawHeader(GuiGraphics g, int y) {
        boolean searching = searchBox != null &&
                !searchBox.getValue().isBlank();

        text(g, searching ? "Search results" : CATEGORIES[selected],
                cx, y + 19, alpha(WHITE), true);

        text(g, "LMB toggle · RMB settings",
                cx, y + 33, alpha(MUTED), false);

        rounded(g, px + pw - 123, y + 15, 108, 23,
                7, alpha(0x77373D4D));
        text(g, "CRIMSON", px + pw - 108, y + 23,
                alpha(MUTED), false);
    }

    private void drawCards(GuiGraphics g, int mx, int my, int y) {
        List<Feature> visible = filteredFeatures();

        if (searchBox != null && searchBox.getValue().isBlank()
                && selected == 5) {
            text(g, "CONFIGURATION", cx, y + 85,
                    alpha(WHITE), true);
            text(g, "Settings are saved automatically.",
                    cx, y + 104, alpha(MUTED), false);
            return;
        }

        if (visible.isEmpty()) {
            text(g, "No functions found",
                    cx + 8, y + 91, alpha(MUTED), false);
            return;
        }

        int columns = cw > 300 ? 2 : 1;
        int gap = 8;
        int cardW = (cw - gap * (columns - 1)) / columns;
        int cardH = 53;

        for (int i = 0; i < visible.size(); i++) {
            Feature f = visible.get(i);

            int col = i % columns;
            int row = i / columns;
            int x = cx + col * (cardW + gap);
            int cy = y + 77 + row * (cardH + 8);

            if (cy + cardH > y + ph - 8) continue;

            boolean enabled = f.get.getAsBoolean();
            boolean hover = hit(mx, my, x, cy, cardW, cardH);
            boolean isSettings = settingsFeature == f;

            rounded(g, x, cy, cardW, cardH, 8,
                    alpha(enabled ? 0x883C202D : 0x66343A4B));
            rounded(g, x, cy, cardW, 1, 1,
                    alpha(enabled || isSettings ? 0xCCFF5365 : 0x55727A8C));

            text(g, f.name, x + 10, cy + 11,
                    alpha(WHITE), true);

            String desc = f.desc.length() > 23
                    ? f.desc.substring(0, 20) + "..."
                    : f.desc;

            text(g, desc, x + 10, cy + 27,
                    alpha(MUTED), false);

            int sx = x + cardW - 34;
            int sy = cy + 10;

            rounded(g, sx, sy, 25, 13, 7,
                    alpha(enabled ? 0xFFFF4355 : 0xFF555A68));
            rounded(g, enabled ? sx + 14 : sx + 2,
                    sy + 2, 9, 9, 5, alpha(WHITE));

            if (hover || isSettings) {
                rounded(g, x + 5, cy + cardH - 2,
                        cardW - 10, 1, 1, alpha(RED));
            }
        }
    }

    private void drawSettingsPanel(GuiGraphics g, int mx, int my, int y) {
        int panelW = 190;
        int panelX = px + pw + 8;
        int panelY = y + 40;
        int panelH = ph - 50;

        if (panelX + panelW > width - 8) {
            panelX = px - panelW - 8;
        }

        rounded(g, panelX + 2, panelY + 3, panelW, panelH, 12, 0x66000000);
        rounded(g, panelX, panelY, panelW, panelH, 12, alpha(0xF0111521));
        rounded(g, panelX + 1, panelY + 1, panelW - 2, panelH - 2, 11, alpha(0xE01B2030));
        rounded(g, panelX + 10, panelY + 1, panelW - 20, 2, 1, alpha(RED));

        text(g, settingsFeature.name, panelX + 12, panelY + 12, alpha(WHITE), true);
        text(g, "Settings", panelX + 12, panelY + 26, alpha(MUTED), false);

        int ly = panelY + 48;
        int left = panelX + 12;
        int right = panelX + panelW - 12;

        // Generic toggles depending on feature
        if (settingsFeature.name.equals("No Render")) {
            ly = toggleRow(g, "Hide Fire", () -> cfg.hideFire, v -> cfg.hideFire = v, left, right, ly, mx, my);
            ly = toggleRow(g, "Hide Hurt Cam", () -> cfg.hideHurtCamera, v -> cfg.hideHurtCamera = v, left, right, ly, mx, my);
            ly = toggleRow(g, "Hide Portal", () -> cfg.hidePortal, v -> cfg.hidePortal = v, left, right, ly, mx, my);
            ly = toggleRow(g, "Hide Pumpkin", () -> cfg.hidePumpkin, v -> cfg.hidePumpkin = v, left, right, ly, mx, my);
            ly = toggleRow(g, "Hide Vignette", () -> cfg.hideVignette, v -> cfg.hideVignette = v, left, right, ly, mx, my);
            ly = toggleRow(g, "Hide Nausea", () -> cfg.hideNausea, v -> cfg.hideNausea = v, left, right, ly, mx, my);
            ly = toggleRow(g, "Hide Weather", () -> cfg.hideWeather, v -> cfg.hideWeather = v, left, right, ly, mx, my);
            ly = toggleRow(g, "Hide Particles", () -> cfg.hideParticles, v -> cfg.hideParticles = v, left, right, ly, mx, my);
        } else if (settingsFeature.name.equals("Target ESP")) {
            ly = toggleRow(g, "Ring", () -> cfg.targetEspRing, v -> cfg.targetEspRing = v, left, right, ly, mx, my);
            ly = toggleRow(g, "Particles", () -> cfg.targetEspParticles, v -> cfg.targetEspParticles = v, left, right, ly, mx, my);
        } else if (settingsFeature.name.equals("Hit Sound")) {
            text(g, "Type: " + cfg.hitSoundType, left, ly, alpha(WHITE), false);
            ly += 16;
            text(g, "Volume: " + String.format("%.1f", cfg.hitSoundVolume), left, ly, alpha(MUTED), false);
            ly += 18;
            text(g, "LMB type+ · RMB type-", left, ly, alpha(MUTED), false);
        } else if (settingsFeature.name.equals("Custom Crosshair")) {
            text(g, "Style: " + cfg.crosshairStyle, left, ly, alpha(WHITE), false);
            ly += 16;
            text(g, "Size: " + String.format("%.1f", cfg.crosshairSize), left, ly, alpha(MUTED), false);
            ly += 16;
            text(g, "Gap: " + String.format("%.1f", cfg.crosshairGap), left, ly, alpha(MUTED), false);
        } else if (settingsFeature.name.equals("Jump Circle")) {
            ly = toggleRow(g, "Fade", () -> cfg.jumpCircleFade, v -> cfg.jumpCircleFade = v, left, right, ly, mx, my);
            text(g, "Size: " + String.format("%.1f", cfg.jumpCircleSize), left, ly, alpha(MUTED), false);
        } else if (settingsFeature.name.equals("Custom Hitbox")) {
            text(g, "Width: " + String.format("%.1f", cfg.hitboxWidth), left, ly, alpha(MUTED), false);
        } else if (settingsFeature.name.equals("Block Overlay")) {
            ly = toggleRow(g, "Gradient / RGB", () -> cfg.blockOverlayGradient, v -> cfg.blockOverlayGradient = v, left, right, ly, mx, my);
            text(g, "Style: " + cfg.blockOverlayStyle, left, ly, alpha(WHITE), false);
            ly += 16;
            text(g, "Width: " + String.format("%.1f", cfg.blockOverlayWidth), left, ly, alpha(MUTED), false);
        } else if (settingsFeature.name.equals("Custom Sky")) {
            text(g, "Style: " + cfg.skyStyle, left, ly, alpha(WHITE), false);
            ly += 16;
            text(g, "Color set in config", left, ly, alpha(MUTED), false);
        } else {
            text(g, "No extra settings", left, ly, alpha(MUTED), false);
        }

        // Close hint
        text(g, "RMB outside to close", panelX + 12, panelY + panelH - 18, alpha(MUTED), false);
    }

    private int toggleRow(GuiGraphics g, String label, BooleanSupplier get, Consumer<Boolean> set,
                          int left, int right, int y, int mx, int my) {
        boolean on = get.getAsBoolean();
        text(g, label, left, y + 2, alpha(WHITE), false);

        int sx = right - 28;
        rounded(g, sx, y, 25, 13, 7, alpha(on ? 0xFFFF4355 : 0xFF555A68));
        rounded(g, on ? sx + 14 : sx + 2, y + 2, 9, 9, 5, alpha(WHITE));

        return y + 20;
    }

    private int widthOf(String s) {
        return font.width(Component.literal(s).setStyle(
                Style.EMPTY.withFont(new FontDescription.Resource(FONT))
        ));
    }

    private boolean hit(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w &&
                my >= y && my <= y + h;
    }

    private int alpha(int color) {
        return ((int) (((color >>> 24) & 255) * anim) << 24)
                | (color & 0xFFFFFF);
    }

    private static void rounded(GuiGraphics g, int x, int y,
                                int w, int h, int r, int color) {
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

    @Override
    public boolean mouseClicked(MouseButtonEvent event,
                                boolean doubleClick) {
        if (searchBox != null && searchBox.isMouseOver(event.x(), event.y())) {
            return super.mouseClicked(event, doubleClick);
        }

        int mx = (int) event.x();
        int my = (int) event.y();
        int button = event.button();

        // Category sidebar
        if (button == 0) {
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (hit(mx, my, px + 8, py + 57 + i * 34,
                        sidebar - 16, 28)) {
                    selected = i;
                    settingsFeature = null;
                    if (searchBox != null) searchBox.setValue("");
                    return true;
                }
            }
        }

        // Settings panel clicks
        if (settingsFeature != null) {
            int panelW = 190;
            int panelX = px + pw + 8;
            int panelY = py + 40;
            int panelH = ph - 50;
            if (panelX + panelW > width - 8) panelX = px - panelW - 8;

            if (hit(mx, my, panelX, panelY, panelW, panelH)) {
                // Handle specific toggles inside panel
                handleSettingsClick(mx, my, panelX, panelY, button);
                cfg.save();
                return true;
            } else if (button == 1) {
                // RMB outside closes settings
                settingsFeature = null;
                return true;
            }
        }

        List<Feature> visible = filteredFeatures();
        int columns = cw > 300 ? 2 : 1;
        int gap = 8;
        int cardW = (cw - gap * (columns - 1)) / columns;
        int cardH = 53;

        for (int i = 0; i < visible.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int x = cx + col * (cardW + gap);
            int cy = py + 77 + row * (cardH + 8);

            if (hit(mx, my, x, cy, cardW, cardH)) {
                Feature f = visible.get(i);

                if (button == 0) {
                    // LMB = toggle
                    f.set.accept(!f.get.getAsBoolean());
                    cfg.save();
                    return true;
                } else if (button == 1) {
                    // RMB = open settings side panel
                    settingsFeature = (settingsFeature == f) ? null : f;
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private void handleSettingsClick(int mx, int my, int panelX, int panelY, int button) {
        int ly = panelY + 48;
        int left = panelX + 12;
        int right = panelX + 190 - 12;

        if (settingsFeature.name.equals("No Render")) {
            ly = tryToggle(mx, my, ly, "Hide Fire", () -> cfg.hideFire, v -> cfg.hideFire = v, left, right);
            ly = tryToggle(mx, my, ly, "Hide Hurt Cam", () -> cfg.hideHurtCamera, v -> cfg.hideHurtCamera = v, left, right);
            ly = tryToggle(mx, my, ly, "Hide Portal", () -> cfg.hidePortal, v -> cfg.hidePortal = v, left, right);
            ly = tryToggle(mx, my, ly, "Hide Pumpkin", () -> cfg.hidePumpkin, v -> cfg.hidePumpkin = v, left, right);
            ly = tryToggle(mx, my, ly, "Hide Vignette", () -> cfg.hideVignette, v -> cfg.hideVignette = v, left, right);
            ly = tryToggle(mx, my, ly, "Hide Nausea", () -> cfg.hideNausea, v -> cfg.hideNausea = v, left, right);
            ly = tryToggle(mx, my, ly, "Hide Weather", () -> cfg.hideWeather, v -> cfg.hideWeather = v, left, right);
            tryToggle(mx, my, ly, "Hide Particles", () -> cfg.hideParticles, v -> cfg.hideParticles = v, left, right);
        } else if (settingsFeature.name.equals("Target ESP")) {
            ly = tryToggle(mx, my, ly, "Ring", () -> cfg.targetEspRing, v -> cfg.targetEspRing = v, left, right);
            tryToggle(mx, my, ly, "Particles", () -> cfg.targetEspParticles, v -> cfg.targetEspParticles = v, left, right);
        } else if (settingsFeature.name.equals("Hit Sound")) {
            if (button == 0) cfg.hitSoundType = (cfg.hitSoundType + 1) % 7;
            else if (button == 1) cfg.hitSoundType = (cfg.hitSoundType + 6) % 7;
        } else if (settingsFeature.name.equals("Jump Circle")) {
            tryToggle(mx, my, ly, "Fade", () -> cfg.jumpCircleFade, v -> cfg.jumpCircleFade = v, left, right);
        } else if (settingsFeature.name.equals("Block Overlay")) {
            tryToggle(mx, my, ly, "Gradient / RGB", () -> cfg.blockOverlayGradient, v -> cfg.blockOverlayGradient = v, left, right);
        }
    }

    private int tryToggle(int mx, int my, int y, String label,
                          BooleanSupplier get, Consumer<Boolean> set,
                          int left, int right) {
        int sx = right - 28;
        if (hit(mx, my, sx, y, 25, 13)) {
            set.accept(!get.getAsBoolean());
        }
        return y + 20;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record Feature(String name, String desc,
                           BooleanSupplier get, Consumer<Boolean> set,
                           int category) {}
    }
