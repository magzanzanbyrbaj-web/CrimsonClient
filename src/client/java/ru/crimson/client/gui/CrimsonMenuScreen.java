
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
import net.minecraft.client.renderer.RenderPipelines;
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
    private static final Identifier LOGO =
            Identifier.fromNamespaceAndPath("crimsonclient", "crimson_logo.png");

    private static final int RED = 0xFFFF4055;
    private static final int WHITE = 0xFFF5F3F7;
    private static final int MUTED = 0xFF9295A5;

    private static final String[] CATEGORIES = {
            "Combat", "Movement", "Player", "Visuals", "Client", "Configs"
    };

    private final CrimsonConfig cfg;
    private final long openedAt = System.currentTimeMillis();
    private final List<Feature> features = new ArrayList<>();

    private EditBox searchBox;
    private int selected = 3;
    private float anim = 1f;

    private int px, py, pw, ph;
    private int sidebar, cx, cw;

    private Feature settingsFeature;

    public CrimsonMenuScreen() {
        super(Component.literal("Crimson Client"));
        cfg = CrimsonClientClient.config;
        buildFeatures();
    }

    private void buildFeatures() {
        add("Target HUD", "Target health and distance", () -> cfg.targetHud, v -> cfg.targetHud = v, 0);
        add("Hit Particles", "Visual hit particles", () -> cfg.hitParticles, v -> cfg.hitParticles = v, 0);
        add("Damage Numbers", "Damage number visuals", () -> cfg.damageNumbers, v -> cfg.damageNumbers = v, 0);
        add("Hitmarker", "Hit confirmation indicator", () -> cfg.hitmarker, v -> cfg.hitmarker = v, 0);
        add("Critical Effects", "Critical hit visuals", () -> cfg.criticalEffects, v -> cfg.criticalEffects = v, 0);
        add("Target ESP", "Particles and ring on hit target", () -> cfg.targetEsp, v -> cfg.targetEsp = v, 0);
        add("Hit Sound", "Custom sound feedback", () -> cfg.hitSound, v -> cfg.hitSound = v, 0);

        add("Armor HUD", "Equipment display", () -> cfg.armorHud, v -> cfg.armorHud = v, 2);
        add("Totem Counter", "Inventory totem count", () -> cfg.totemCounter, v -> cfg.totemCounter = v, 2);
        add("Inventory HUD", "Inventory item display", () -> cfg.inventoryHud, v -> cfg.inventoryHud = v, 2);
        add("Potion HUD", "Active potion effects", () -> cfg.potionHud, v -> cfg.potionHud = v, 2);
        add("Durability HUD", "Item durability", () -> cfg.durabilityHud, v -> cfg.durabilityHud = v, 2);

        add("Smooth Sword", "Sword animation", () -> cfg.smoothSword, v -> cfg.smoothSword = v, 3);
        add("Item Switch", "Item switch animation", () -> cfg.itemSwitchAnim, v -> cfg.itemSwitchAnim = v, 3);
        add("Totem Animation", "Totem animation", () -> cfg.totemAnimation, v -> cfg.totemAnimation = v, 3);
        add("Custom Crosshair", "Custom crosshair", () -> cfg.customCrosshair, v -> cfg.customCrosshair = v, 3);
        add("Custom Sky", "Sky color and style", () -> cfg.customSky, v -> cfg.customSky = v, 3);
        add("Inventory Blur", "Inventory background effect", () -> cfg.inventoryBlur, v -> cfg.inventoryBlur = v, 3);
        add("Jump Circle", "Circle under feet on jump", () -> cfg.jumpCircle, v -> cfg.jumpCircle = v, 3);
        add("Custom Hitbox", "Player hitboxes", () -> cfg.customHitbox, v -> cfg.customHitbox = v, 3);
        add("Block Overlay", "Block outline style", () -> cfg.blockOverlay, v -> cfg.blockOverlay = v, 3);
        add("No Render", "Hide selected visual effects", () -> cfg.noRender, v -> cfg.noRender = v, 3);

        add("Watermark", "Logo, FPS and ping", () -> cfg.watermark, v -> cfg.watermark = v, 4);
        add("Keystrokes", "Keyboard display", () -> cfg.keystrokes, v -> cfg.keystrokes = v, 4);
        add("Module List", "Enabled module list", () -> cfg.moduleList, v -> cfg.moduleList = v, 4);
        add("Fullbright", "World brightness", () -> cfg.fullbright, v -> cfg.fullbright = v, 4);
        add("Reduce Particles", "Reduce particle rendering", () -> cfg.reduceParticles, v -> cfg.reduceParticles = v, 4);
        add("Optimization", "Client optimization", () -> cfg.optimized, v -> cfg.optimized = v, 4);
        add("Menu Animation", "Menu animations", () -> cfg.glassAnimation, v -> cfg.glassAnimation = v, 4);
    }

    private void add(String name, String desc, BooleanSupplier get,
                     Consumer<Boolean> set, int category) {
        features.add(new Feature(name, desc, get, set, category));
    }

    private void layout() {
        pw = Math.min(520, width - 20);
        ph = Math.min(340, height - 20);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        sidebar = 112;
        cx = px + sidebar + 16;
        cw = pw - sidebar - 28;
    }

    @Override
    protected void init() {
        layout();
        searchBox = new EditBox(font, cx, py + 47, cw, 19, Component.literal("Search"));
        searchBox.setHint(Component.literal("Search functions..."));
        searchBox.setMaxLength(64);
        addRenderableWidget(searchBox);
    }

    private void text(GuiGraphics g, String value, int x, int y, int color, boolean shadow) {
        Component component = Component.literal(value)
                .setStyle(Style.EMPTY.withFont(new FontDescription.Resource(FONT)));
        g.drawString(font, component, x, y, color, shadow);
    }

    private List<Feature> filteredFeatures() {
        List<Feature> result = new ArrayList<>();
        String query = searchBox == null ? "" :
                searchBox.getValue().trim().toLowerCase(Locale.ROOT);

        for (Feature f : features) {
            if (query.isEmpty()) {
                if (f.category == selected) result.add(f);
            } else if (f.name.toLowerCase(Locale.ROOT).contains(query)
                    || f.desc.toLowerCase(Locale.ROOT).contains(query)) {
                result.add(f);
            }
        }
        return result;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        layout();

        float t = Mth.clamp((System.currentTimeMillis() - openedAt) / 260f, 0f, 1f);
        anim = cfg.glassAnimation ? 1f - (float) Math.pow(1f - t, 3) : 1f;

        g.fill(0, 0, width, height, 0x99030812);

        int lift = (int) ((1f - anim) * 8);
        int y = py + lift;

        rounded(g, px + 3, y + 5, pw, ph, 15, 0x66000000);
        rounded(g, px, y, pw, ph, 15, alpha(0xED111521));
        rounded(g, px + 1, y + 1, pw - 2, ph - 2, 14, alpha(0xE21B2030));
        rounded(g, px + 14, y + 1, pw - 28, 2, 1, alpha(RED));
        rounded(g, px + 1, y + 1, sidebar, ph - 2, 13, alpha(0xB80B0F19));

        g.fill(px + sidebar, y + 16, px + sidebar + 1, y + ph - 16, alpha(0x33FFFFFF));

        drawSidebar(g, mouseX, mouseY, y);
        drawHeader(g, y);
        drawCards(g, mouseX, mouseY, y);

        if (settingsFeature != null) drawSettingsPanel(g, mouseX, mouseY, y);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawSidebar(GuiGraphics g, int mx, int my, int y) {
        rounded(g, px + 13, y + 14, 27, 27, 8, alpha(0xFF55202F));
        g.blit(RenderPipelines.GUI_TEXTURED, LOGO,
                px + 13, y + 14, 0f, 0f, 27, 27, 128, 128);

        text(g, "CRIMSON", px + 46, y + 17, alpha(WHITE), true);
        text(g, "CLIENT", px + 46, y + 29, alpha(MUTED), false);

        for (int i = 0; i < CATEGORIES.length; i++) {
            int bx = px + 8;
            int by = y + 57 + i * 34;
            int bw = sidebar - 16;
            boolean active = selected == i;
            boolean hover = hit(mx, my, bx, by, bw, 28);

            if (active) {
                rounded(g, bx, by, bw, 28, 7, alpha(0xAA482331));
                rounded(g, bx, by + 4, 2, 20, 1, alpha(RED));
            } else if (hover) {
                rounded(g, bx, by, bw, 28, 7, alpha(0x553B4052));
            }

            text(g, CATEGORIES[i], bx + 12, by + 10,
                    alpha(active ? WHITE : MUTED), false);

            int count = 0;
            for (Feature f : features) if (f.category == i) count++;

            String n = String.valueOf(count);
            text(g, n, bx + bw - widthOf(n) - 9, by + 10, alpha(MUTED), false);
        }

        int profileY = y + ph - 43;
        rounded(g, px + 8, profileY, sidebar - 16, 33, 7, alpha(0x553C4050));
        rounded(g, px + 13, profileY + 5, 22, 22, 6, alpha(0xFF542432));
        g.blit(RenderPipelines.GUI_TEXTURED, LOGO,
                px + 13, profileY + 5, 0f, 0f, 22, 22, 128, 128);

        String player = Minecraft.getInstance().player == null
                ? "Player" : Minecraft.getInstance().player.getName().getString();

        text(g, player.length() > 12 ? player.substring(0, 12) : player,
                px + 41, profileY + 7, alpha(WHITE), false);
        text(g, "Crimson user", px + 41, profileY + 19, alpha(MUTED), false);
    }

    private void drawHeader(GuiGraphics g, int y) {
        boolean searching = searchBox != null && !searchBox.getValue().isBlank();
        text(g, searching ? "Search results" : CATEGORIES[selected],
                cx, y + 18, alpha(WHITE), true);
        text(g, "LMB toggle  |  RMB settings", cx, y + 31, alpha(MUTED), false);

        rounded(g, px + pw - 105, y + 14, 90, 22, 7, alpha(0x77373D4D));
        text(g, "CRIMSON", px + pw - 92, y + 21, alpha(MUTED), false);
    }

    private void drawCards(GuiGraphics g, int mx, int my, int y) {
        List<Feature> visible = filteredFeatures();

        if (visible.isEmpty()) {
            text(g, "No functions found", cx + 8, y + 90, alpha(MUTED), false);
            return;
        }

        int columns = cw > 300 ? 2 : 1;
        int gap = 7;
        int cardW = (cw - gap * (columns - 1)) / columns;
        int cardH = 49;

        for (int i = 0; i < visible.size(); i++) {
            Feature f = visible.get(i);
            int col = i % columns;
            int row = i / columns;
            int x = cx + col * (cardW + gap);
            int cy = y + 74 + row * (cardH + 7);

            if (cy + cardH > y + ph - 7) continue;

            boolean enabled = f.get.getAsBoolean();
            boolean hover = hit(mx, my, x, cy, cardW, cardH);
            boolean settings = settingsFeature == f;

            rounded(g, x, cy, cardW, cardH, 8,
                    alpha(enabled ? 0x883C202D : 0x66343A4B));
            rounded(g, x, cy, cardW, 1, 1,
                    alpha(enabled || settings ? 0xCCFF5365 : 0x55727A8C));

            text(g, f.name, x + 9, cy + 10, alpha(WHITE), true);

            String desc = f.desc.length() > 22 ? f.desc.substring(0, 19) + "..." : f.desc;
            text(g, desc, x + 9, cy + 26, alpha(MUTED), false);

            int sx = x + cardW - 32;
            int sy = cy + 9;
            rounded(g, sx, sy, 24, 13, 7,
                    alpha(enabled ? 0xFFFF4355 : 0xFF555A68));
            rounded(g, enabled ? sx + 13 : sx + 2, sy + 2, 9, 9, 5, alpha(WHITE));

            if (hover || settings) {
                rounded(g, x + 5, cy + cardH - 2, cardW - 10, 1, 1, alpha(RED));
            }
        }
    }

    private void drawSettingsPanel(GuiGraphics g, int mx, int my, int y) {
        int panelW = 142;
        int panelX = px + pw - panelW - 9;
        int panelY = y + 43;
        int panelH = Math.min(250, ph - 52);

        rounded(g, panelX + 2, panelY + 3, panelW, panelH, 9, 0x66000000);
        rounded(g, panelX, panelY, panelW, panelH, 9, alpha(0xF0111521));
        rounded(g, panelX + 1, panelY + 1, panelW - 2, panelH - 2, 8, alpha(0xE01B2030));
        rounded(g, panelX + 9, panelY + 1, panelW - 18, 2, 1, alpha(RED));

        text(g, settingsFeature.name, panelX + 9, panelY + 9, alpha(WHITE), true);
        text(g, "SETTINGS", panelX + 9, panelY + 22, alpha(MUTED), false);

        int ly = panelY + 40;
        int left = panelX + 9;
        int right = panelX + panelW - 9;

        switch (settingsFeature.name) {
            case "No Render" -> {
                drawToggle(g, "Hide Fire", cfg.hideFire, left, right, ly); ly += 19;
                drawToggle(g, "Hurt Camera", cfg.hideHurtCamera, left, right, ly); ly += 19;
                drawToggle(g, "Portal", cfg.hidePortal, left, right, ly); ly += 19;
                drawToggle(g, "Pumpkin", cfg.hidePumpkin, left, right, ly); ly += 19;
                drawToggle(g, "Vignette", cfg.hideVignette, left, right, ly); ly += 19;
                drawToggle(g, "Nausea", cfg.hideNausea, left, right, ly); ly += 19;
                drawToggle(g, "Weather", cfg.hideWeather, left, right, ly); ly += 19;
                drawToggle(g, "Particles", cfg.hideParticles, left, right, ly);
            }
            case "Target ESP" -> {
                drawToggle(g, "Ring", cfg.targetEspRing, left, right, ly); ly += 20;
                drawToggle(g, "Particles", cfg.targetEspParticles, left, right, ly);
            }
            case "Hit Sound" -> {
                text(g, "Sound: " + soundName(cfg.hitSoundType), left, ly, alpha(WHITE), false);
                text(g, "LMB / RMB changes", left, ly + 14, alpha(MUTED), false);
                ly += 37;
                text(g, "Volume", left, ly, alpha(WHITE), false);
                text(g, Math.round(cfg.hitSoundVolume * 100) + "%", right - 28, ly, alpha(RED), false);
                ly += 18;
                drawAdjust(g, left, right, ly);
            }
            case "Critical Effects" -> {
                text(g, "Style:", left, ly, alpha(MUTED), false);
                text(g, criticalName(cfg.criticalStyle), left, ly + 17, alpha(RED), false);
                text(g, "LMB / RMB changes", left, ly + 36, alpha(MUTED), false);
            }
            case "Jump Circle" -> {
                drawToggle(g, "Fade", cfg.jumpCircleFade, left, right, ly);
                ly += 22;
                text(g, "Thickness", left, ly, alpha(WHITE), false);
                text(g, String.format(Locale.ROOT, "%.1f", cfg.jumpCircleThickness),
                        right - 25, ly, alpha(RED), false);
                ly += 18;
                drawAdjust(g, left, right, ly);
                ly += 27;
                text(g, "Size: " + String.format(Locale.ROOT, "%.1f", cfg.jumpCircleSize),
                        left, ly, alpha(MUTED), false);
            }
            case "Custom Crosshair" -> {
                text(g, "Style: " + cfg.crosshairStyle, left, ly, alpha(WHITE), false);
                ly += 18;
                text(g, "Size: " + cfg.crosshairSize, left, ly, alpha(MUTED), false);
                ly += 18;
                text(g, "Gap: " + cfg.crosshairGap, left, ly, alpha(MUTED), false);
            }
            case "Custom Hitbox" ->
                    text(g, "Width: " + cfg.hitboxWidth, left, ly, alpha(WHITE), false);
            case "Block Overlay" -> {
                drawToggle(g, "Gradient / RGB", cfg.blockOverlayGradient, left, right, ly);
                ly += 21;
                text(g, "Style: " + cfg.blockOverlayStyle, left, ly, alpha(WHITE), false);
                ly += 18;
                text(g, "Width: " + cfg.blockOverlayWidth, left, ly, alpha(MUTED), false);
            }
            case "Custom Sky" -> {
                text(g, "Style: " + cfg.skyStyle, left, ly, alpha(WHITE), false);
                ly += 18;
                text(g, "Color in config", left, ly, alpha(MUTED), false);
            }
            default -> text(g, "No extra settings", left, ly, alpha(MUTED), false);
        }

        text(g, "RMB: close", panelX + 9, panelY + panelH - 14, alpha(MUTED), false);
    }

    private void drawToggle(GuiGraphics g, String label, boolean on,
                            int left, int right, int y) {
        text(g, label, left, y + 2, alpha(WHITE), false);
        int sx = right - 25;
        rounded(g, sx, y, 23, 12, 6,
                alpha(on ? 0xFFFF4355 : 0xFF555A68));
        rounded(g, on ? sx + 12 : sx + 2, y + 2, 8, 8, 4, alpha(WHITE));
    }

    private void drawAdjust(GuiGraphics g, int left, int right, int y) {
        rounded(g, left, y, 23, 15, 5, alpha(0xFF414858));
        rounded(g, right - 23, y, 23, 15, 5, alpha(0xFF414858));
        text(g, "-", left + 9, y + 3, alpha(WHITE), false);
        text(g, "+", right - 15, y + 3, alpha(WHITE), false);
    }

    private String soundName(int id) {
        return switch (Math.floorMod(id, 7)) {
            case 1 -> "Critical";
            case 2 -> "Strong";
            case 3 -> "Knockback";
            case 4 -> "Arrow";
            case 5 -> "XP";
            case 6 -> "Pling";
            default -> "Weak";
        };
    }

    private String criticalName(int id) {
        return switch (Math.floorMod(id, 4)) {
            case 1 -> "Ghosts";
            case 2 -> "Spiral";
            case 3 -> "Burst";
            default -> "Staircase";
        };
    }

    private int widthOf(String s) {
        return font.width(Component.literal(s).setStyle(
                Style.EMPTY.withFont(new FontDescription.Resource(FONT))));
    }

    private boolean hit(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private int alpha(int color) {
        return ((int) (((color >>> 24) & 255) * anim) << 24) | (color & 0xFFFFFF);
    }

    private static void rounded(GuiGraphics g, int x, int y,
                                int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(1, Math.min(r, Math.min(w / 2, h / 2)));

        for (int row = 0; row < r; row++) {
            int inset = (int) Math.ceil(
                    r - Math.sqrt(r * r - (r - row) * (r - row)));
            g.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
            g.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, color);
        }
        g.fill(x, y + r, x + w, y + h - r, color);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mx = (int) event.x();
        int my = (int) event.y();
        int button = event.button();

        if (searchBox != null && searchBox.isMouseOver(event.x(), event.y())) {
            return super.mouseClicked(event, doubleClick);
        }

        if (button == 0) {
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (hit(mx, my, px + 8, py + 57 + i * 34, sidebar - 16, 28)) {
                    selected = i;
                    settingsFeature = null;
                    if (searchBox != null) searchBox.setValue("");
                    return true;
                }
            }
        }

        if (settingsFeature != null) {
            int panelW = 142;
            int panelX = px + pw - panelW - 9;
            int panelY = py + 43;
            int left = panelX + 9;
            int right = panelX + panelW - 9;

            if (hit(mx, my, panelX, panelY, panelW, Math.min(250, ph - 52))) {
                int ly = panelY + 40;

                switch (settingsFeature.name) {
                    case "No Render" -> {
                        if (rowHit(mx, my, right, ly)) cfg.hideFire = !cfg.hideFire; ly += 19;
                        if (rowHit(mx, my, right, ly)) cfg.hideHurtCamera = !cfg.hideHurtCamera; ly += 19;
                        if (rowHit(mx, my, right, ly)) cfg.hidePortal = !cfg.hidePortal; ly += 19;
                        if (rowHit(mx, my, right, ly)) cfg.hidePumpkin = !cfg.hidePumpkin; ly += 19;
                        if (rowHit(mx, my, right, ly)) cfg.hideVignette = !cfg.hideVignette; ly += 19;
                        if (rowHit(mx, my, right, ly)) cfg.hideNausea = !cfg.hideNausea; ly += 19;
                        if (rowHit(mx, my, right, ly)) cfg.hideWeather = !cfg.hideWeather; ly += 19;
                        if (rowHit(mx, my, right, ly)) cfg.hideParticles = !cfg.hideParticles;
                    }
                    case "Target ESP" -> {
                        if (rowHit(mx, my, right, ly)) cfg.targetEspRing = !cfg.targetEspRing;
                        else if (rowHit(mx, my, right, ly + 20)) cfg.targetEspParticles = !cfg.targetEspParticles;
                    }
                    case "Hit Sound" -> {
                        if (my < panelY + 40 + 34) {
                            cfg.hitSoundType = Math.floorMod(cfg.hitSoundType + (button == 1 ? 6 : 1), 7);
                        } else if (my >= panelY + 40 + 55 && my <= panelY + 40 + 100) {
                            cfg.hitSoundVolume = Mth.clamp(
                                    cfg.hitSoundVolume + (mx < panelX + 75 ? -0.1f : 0.1f),
                                    0f, 1f);
                        }
                    }
                    case "Critical Effects" ->
                            cfg.criticalStyle = Math.floorMod(cfg.criticalStyle + (button == 1 ? 3 : 1), 4);
                    case "Jump Circle" -> {
                        if (rowHit(mx, my, right, ly)) cfg.jumpCircleFade = !cfg.jumpCircleFade;
                        else if (my >= panelY + 40 + 22 && my <= panelY + 40 + 75) {
                            cfg.jumpCircleThickness = Mth.clamp(
                                    cfg.jumpCircleThickness + (mx < panelX + 75 ? -0.5f : 0.5f),
                                    0.5f, 10f);
                        }
                    }
                    case "Block Overlay" ->
                            cfg.blockOverlayGradient = !cfg.blockOverlayGradient;
                }

                cfg.save();
                return true;
            }

            if (button == 1) {
                settingsFeature = null;
                return true;
            }
        }

        List<Feature> visible = filteredFeatures();
        int columns = cw > 300 ? 2 : 1;
        int gap = 7;
        int cardW = (cw - gap * (columns - 1)) / columns;
        int cardH = 49;

        for (int i = 0; i < visible.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int x = cx + col * (cardW + gap);
            int cy = py + 74 + row * (cardH + 7);

            if (hit(mx, my, x, cy, cardW, cardH)) {
                Feature f = visible.get(i);
                if (button == 0) {
                    f.set.accept(!f.get.getAsBoolean());
                    cfg.save();
                    return true;
                }
                if (button == 1) {
                    settingsFeature = settingsFeature == f ? null : f;
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private boolean rowHit(int mx, int my, int right, int y) {
        return hit(mx, my, right - 30, y, 30, 17);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record Feature(String name, String desc,
                           BooleanSupplier get, Consumer<Boolean> set,
                           int category) {}
}
