
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

    private static final Identifier LOGO =
            Identifier.fromNamespaceAndPath("crimsonclient", "crimson_logo.png");

    private static final int WHITE = 0xFFF0F0F2;
    private static final int MUTED = 0xFF777982;
    private static final int RED = 0xFFE64B5D;

    private static final String[] CATEGORIES = {
            "Render", "Player", "Display", "Configs"
    };

    private final CrimsonConfig cfg;
    private final List<Feature> features = new ArrayList<>();

    private EditBox searchBox;

    private int selected = 0;
    private int scrollOffset = 0;

    private int px, py, pw, ph;
    private int sidebar, cx, cw;
    private int cardHeight = 51;
    private int cardGap = 7;

    private Feature settingsFeature;

    public CrimsonMenuScreen() {
        super(Component.literal("Crimson Client"));
        cfg = CrimsonClientClient.config;
        buildFeatures();
    }

    private void buildFeatures() {
        // RENDER
        add("HitParticles", "Creates particles after a hit",
                () -> cfg.hitParticles, v -> cfg.hitParticles = v, 0);

        add("CriticalEffects", "Critical hit visual effects",
                () -> cfg.criticalEffects, v -> cfg.criticalEffects = v, 0);

        add("HitSound", "Custom hit sound",
                () -> cfg.hitSound, v -> cfg.hitSound = v, 0);

        add("Hitmarker", "Hit confirmation indicator",
                () -> cfg.hitmarker, v -> cfg.hitmarker = v, 0);

        add("DamageNumbers", "Damage number display",
                () -> cfg.damageNumbers, v -> cfg.damageNumbers = v, 0);

        add("TargetESP", "Visual target highlighting",
                () -> cfg.targetEsp, v -> cfg.targetEsp = v, 0);

        add("CustomCrosshair", "Custom crosshair",
                () -> cfg.customCrosshair, v -> cfg.customCrosshair = v, 0);

        add("CustomSky", "Custom world sky",
                () -> cfg.customSky, v -> cfg.customSky = v, 0);

        add("JumpCircle", "Circle under the player",
                () -> cfg.jumpCircle, v -> cfg.jumpCircle = v, 0);

        add("CustomHitBox", "Custom hitbox rendering",
                () -> cfg.customHitbox, v -> cfg.customHitbox = v, 0);

        add("BlockOverlay", "Custom block outline",
                () -> cfg.blockOverlay, v -> cfg.blockOverlay = v, 0);

        add("NoRender", "Hide selected visual effects",
                () -> cfg.noRender, v -> cfg.noRender = v, 0);

        add("InventoryBlur", "Inventory background blur",
                () -> cfg.inventoryBlur, v -> cfg.inventoryBlur = v, 0);

        add("SmoothSword", "Smooth sword animation",
                () -> cfg.smoothSword, v -> cfg.smoothSword = v, 0);

        add("ItemSwitch", "Item switching animation",
                () -> cfg.itemSwitchAnim, v -> cfg.itemSwitchAnim = v, 0);

        add("TotemAnimation", "Totem animation",
                () -> cfg.totemAnimation, v -> cfg.totemAnimation = v, 0);

        add("ReachDisplay", "Distance to the selected entity",
                () -> cfg.reachDisplay, v -> cfg.reachDisplay = v, 0);

        add("PearlTracker", "Ender pearl tracking",
                () -> cfg.pearlTracker, v -> cfg.pearlTracker = v, 0);

        // PLAYER
        add("TargetHUD", "Target information",
                () -> cfg.targetHud, v -> cfg.targetHud = v, 1);

        add("ArmorHUD", "Armor equipment display",
                () -> cfg.armorHud, v -> cfg.armorHud = v, 1);

        add("TotemCounter", "Totem counter",
                () -> cfg.totemCounter, v -> cfg.totemCounter = v, 1);

        add("InventoryHUD", "Inventory item display",
                () -> cfg.inventoryHud, v -> cfg.inventoryHud = v, 1);

        add("PotionHUD", "Active potion effects",
                () -> cfg.potionHud, v -> cfg.potionHud = v, 1);

        add("DurabilityHUD", "Item durability display",
                () -> cfg.durabilityHud, v -> cfg.durabilityHud = v, 1);

        // DISPLAY
        add("Watermark", "Client name, FPS and ping",
                () -> cfg.watermark, v -> cfg.watermark = v, 2);

        add("Keystrokes", "Keyboard display",
                () -> cfg.keystrokes, v -> cfg.keystrokes = v, 2);

        add("ModuleList", "Enabled module list",
                () -> cfg.moduleList, v -> cfg.moduleList = v, 2);

        // CONFIGS
        add("Fullbright", "World brightness",
                () -> cfg.fullbright, v -> cfg.fullbright = v, 3);

        add("ReduceParticles", "Reduce particle rendering",
                () -> cfg.reduceParticles, v -> cfg.reduceParticles = v, 3);

        add("FPSBoost", "Client performance options",
                () -> cfg.fpsBoost, v -> cfg.fpsBoost = v, 3);

        add("Optimization", "Client optimization",
                () -> cfg.optimized, v -> cfg.optimized = v, 3);

        add("MenuAnimation", "Menu opening animation",
                () -> cfg.glassAnimation, v -> cfg.glassAnimation = v, 3);
    }

    private void add(String name, String desc,
                     BooleanSupplier get, Consumer<Boolean> set,
                     int category) {
        features.add(new Feature(name, desc, get, set, category));
    }

    private void layout() {
        pw = Math.min(width - 24, Math.max(500, (int) (width * 0.532f)));
        ph = Math.min(height - 24, Math.max(340, (int) (height * 0.59f)));

        px = (width - pw) / 2;
        py = (height - ph) / 2;

        sidebar = Math.max(112, (int) (pw * 0.185f));

        cx = px + sidebar + 14;
        cw = pw - sidebar - 28;
    }

    @Override
    protected void init() {
        layout();

        int searchWidth = Math.min(210, Math.max(110, cw / 3));

        searchBox = new EditBox(
                font,
                px + pw - searchWidth - 16,
                py + 15,
                searchWidth,
                25,
                Component.literal("Search")
        );

        searchBox.setHint(Component.literal("Поиск..."));
        searchBox.setMaxLength(64);
        searchBox.setBordered(false);
        searchBox.setTextColor(0xFFBFC0C7);

        addRenderableWidget(searchBox);
    }

    private void text(GuiGraphics g, String value,
                      int x, int y, int color) {
        Component component = Component.literal(value)
                .setStyle(Style.EMPTY.withFont(
                        new FontDescription.Resource(FONT)));

        g.drawString(font, component, x, y, color, false);
    }

    private static void rounded(GuiGraphics g,
                                int x, int y, int w, int h,
                                int radius, int color) {
        if (w <= 0 || h <= 0) return;

        radius = Math.max(1,
                Math.min(radius, Math.min(w / 2, h / 2)));

        for (int row = 0; row < radius; row++) {
            int inset = (int) Math.ceil(
                    radius - Math.sqrt(
                            radius * radius -
                            (radius - row) * (radius - row)
                    )
            );

            g.fill(x + inset, y + row,
                    x + w - inset, y + row + 1, color);

            g.fill(x + inset, y + h - row - 1,
                    x + w - inset, y + h - row, color);
        }

        g.fill(x, y + radius, x + w, y + h - radius, color);
    }

    private boolean hit(int mx, int my,
                        int x, int y, int w, int h) {
        return mx >= x && mx <= x + w
                && my >= y && my <= y + h;
    }

    private List<Feature> filteredFeatures() {
        List<Feature> result = new ArrayList<>();

        String query = searchBox == null ? ""
                : searchBox.getValue().trim().toLowerCase(Locale.ROOT);

        for (Feature feature : features) {
            if (query.isEmpty()) {
                if (feature.category == selected) {
                    result.add(feature);
                }
            } else if (feature.name.toLowerCase(Locale.ROOT).contains(query)
                    || feature.desc.toLowerCase(Locale.ROOT).contains(query)) {
                result.add(feature);
            }
        }

        return result;
    }

    @Override
    public void render(GuiGraphics g, int mouseX,
                       int mouseY, float partialTick) {
        layout();

        g.fill(0, 0, width, height, 0x55000000);

        // Main window
        rounded(g, px + 3, py + 5, pw, ph, 17, 0x55000000);
        rounded(g, px, py, pw, ph, 17, 0xF0090A0C);
        rounded(g, px + 1, py + 1, pw - 2, ph - 2,
                16, 0xF20D0E10);

        // Sidebar
        rounded(g, px + 1, py + 1, sidebar, ph - 2,
                15, 0xFF0B0C0E);

        // Sidebar separator
        g.fill(px + sidebar, py + 13,
                px + sidebar + 1, py + ph - 13, 0x332F3035);

        drawSidebar(g, mouseX, mouseY);
        drawHeader(g, mouseX, mouseY);
        drawCards(g, mouseX, mouseY);

        if (settingsFeature != null) {
            drawSettings(g);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawSidebar(GuiGraphics g, int mx, int my) {
        // Logo
        rounded(g, px + 13, py + 17, 25, 25,
                7, 0xFF191A1E);

        g.blit(
                net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                LOGO,
                px + 13, py + 17,
                0f, 0f, 25, 25, 128, 128
        );

        text(g, "CRIMSON", px + 45, py + 19, WHITE);
        text(g, "1.21.11", px + 45, py + 31, MUTED);

        // Small navigation indicator
        rounded(g, px + 13, py + 69,
                sidebar - 26, 29, 9, 0xFF111216);

        text(g, "‹", px + 24, py + 78, MUTED);
        rounded(g, px + 54, py + 81, 17, 4, 3, WHITE);
        text(g, "›", px + sidebar - 29, py + 78, MUTED);

        for (int i = 0; i < CATEGORIES.length; i++) {
            int bx = px + 13;
            int by = py + 126 + i * 39;
            int bw = sidebar - 26;
            int bh = 29;

            boolean active = selected == i;
            boolean hover = hit(mx, my, bx, by, bw, bh);

            if (active) {
                rounded(g, bx, by, bw, bh,
                        8, 0xFFF5F5F6);
            } else if (hover) {
                rounded(g, bx, by, bw, bh,
                        8, 0xFF17181C);
            }

            int color = active ? 0xFF17181C : 0xFF999BA4;

            String icon = switch (i) {
                case 0 -> "▣";
                case 1 -> "♟";
                case 2 -> "▦";
                default -> "▤";
            };

            text(g, icon, bx + 10, by + 9, color);
            text(g, CATEGORIES[i], bx + 31, by + 9, color);
        }

        // Profile block
        int profileY = py + ph - 43;

        rounded(g, px + 13, profileY,
                sidebar - 26, 31, 8, 0xFF141519);

        rounded(g, px + 18, profileY + 5,
                21, 21, 7, 0xFF393A40);

        Minecraft mc = Minecraft.getInstance();

        String player = mc.player == null
                ? "Player"
                : mc.player.getName().getString();

        if (player.length() > 12) {
            player = player.substring(0, 12);
        }

        text(g, player, px + 44,
                profileY + 7, WHITE);

        text(g, "Crimson user", px + 44,
                profileY + 19, MUTED);

        text(g, "⚙", px + sidebar - 29,
                profileY + 10, MUTED);
    }

    private void drawHeader(GuiGraphics g, int mx, int my) {
        int headerX = cx;
        int headerY = py + 15;

        int searchWidth = Math.min(210, Math.max(110, cw / 3));
        int titleWidth = cw - searchWidth - 10;

        rounded(g, headerX, headerY,
                titleWidth, 27, 8, 0xFF111216);

        text(g, "▣", headerX + 11,
                headerY + 9, 0xFFB9BAC0);

        text(g, CATEGORIES[selected],
                headerX + 31, headerY + 9, WHITE);

        text(g, "Визуальные модули",
                headerX + titleWidth - 125,
                headerY + 9, MUTED);

        int searchX = px + pw - searchWidth - 15;

        rounded(g, searchX, headerY,
                searchWidth, 27, 8, 0xFF111216);

        text(g, "⌕", searchX + searchWidth - 20,
                headerY + 8, MUTED);
    }

    private void drawCards(GuiGraphics g, int mx, int my) {
        List<Feature> visible = filteredFeatures();

        int columns = 2;
        int gap = 9;
        int cardW = (cw - gap) / columns;

        int startY = py + 78;
        int bottom = py + ph - 12;

        int rowsVisible = Math.max(1,
                (bottom - startY + cardGap) / (cardHeight + cardGap));

        int maxOffset = Math.max(0,
                (int) Math.ceil((double) visible.size() / columns)
                        - rowsVisible);

        scrollOffset = Mth.clamp(scrollOffset, 0, maxOffset);

        int startIndex = scrollOffset * columns;
        int endIndex = Math.min(visible.size(),
                startIndex + rowsVisible * columns);

        if (visible.isEmpty()) {
            text(g, "Ничего не найдено",
                    cx + 12, startY + 15, MUTED);
            return;
        }

        for (int i = startIndex; i < endIndex; i++) {
            Feature f = visible.get(i);

            int local = i - startIndex;
            int col = local % columns;
            int row = local / columns;

            int x = cx + col * (cardW + gap);
            int y = startY + row * (cardHeight + cardGap);

            boolean enabled = f.get.getAsBoolean();
            boolean hover = hit(mx, my, x, y, cardW, cardHeight);
            boolean selectedSettings = settingsFeature == f;

            int background = hover
                    ? 0xFF17181C
                    : 0xFF111216;

            rounded(g, x, y, cardW, cardHeight,
                    9, background);

            // Thin card border
            rounded(g, x, y, cardW, 1, 1,
                    selectedSettings ? RED : 0xFF202126);

            rounded(g, x, y + cardHeight - 1,
                    cardW, 1, 1, 0xFF202126);

            text(g, f.name, x + 12,
                    y + 12, enabled ? WHITE : 0xFF9B9CA3);

            String desc = f.desc;
            int maxTextWidth = cardW - 45;

            while (!desc.isEmpty()
                    && font.width(Component.literal(desc)) > maxTextWidth) {
                desc = desc.substring(0, desc.length() - 1);
            }

            text(g, desc, x + 12,
                    y + 31, 0xFF65666D);

            // Settings gear
            int gearX = x + cardW - 27;
            int gearY = y + 9;

            rounded(g, gearX, gearY,
                    17, 17, 5,
                    selectedSettings ? 0xFF393A40 : 0xFF1A1B20);

            text(g, "⚙", gearX + 4,
                    gearY + 4, 0xFF96979E);

            // Small enabled indicator
            if (enabled) {
                rounded(g, x + cardW - 9,
                        y + 3, 3, 3, 2, RED);
            }
        }

        // Scrollbar
        int totalRows = (int) Math.ceil(
                (double) visible.size() / columns);

        if (totalRows > rowsVisible) {
            int trackX = px + pw - 9;
            int trackY = startY;
            int trackH = bottom - startY;

            rounded(g, trackX, trackY,
                    3, trackH, 2, 0xFF202126);

            int thumbH = Math.max(22,
                    trackH * rowsVisible / totalRows);

            int thumbY = trackY
                    + (trackH - thumbH) * scrollOffset
                    / Math.max(1, totalRows - rowsVisible);

            rounded(g, trackX, thumbY,
                    3, thumbH, 2, 0xFF777982);
        }
    }

    private void drawSettings(GuiGraphics g) {
        int panelW = 190;
        int panelH = 90;

        int x = px + pw - panelW - 14;
        int y = py + ph - panelH - 14;

        rounded(g, x + 2, y + 3,
                panelW, panelH, 9, 0x77000000);

        rounded(g, x, y,
                panelW, panelH, 9, 0xFF15161A);

        text(g, settingsFeature.name,
                x + 11, y + 12, WHITE);

        text(g, "НАСТРОЙКИ МОДУЛЯ",
                x + 11, y + 29, MUTED);

        String info = switch (settingsFeature.name) {
            case "HitSound" ->
                    "Sound: " + settingsFeatureValue();
            case "CriticalEffects" ->
                    "Style: " + criticalName();
            case "JumpCircle" ->
                    "Thickness: " + cfg.jumpCircleThickness;
            case "TargetESP" ->
                    "Ring / Particles";
            case "NoRender" ->
                    "Visual effects options";
            default ->
                    "Дополнительные настройки";
        };

        text(g, info, x + 11,
                y + 51, 0xFFD0D0D4);

        text(g, "Нажми ЛКМ для изменения",
                x + 11, y + 69, MUTED);
    }

    private String settingsFeatureValue() {
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
    public boolean mouseClicked(MouseButtonEvent event,
                                boolean doubleClick) {
        int mx = (int) event.x();
        int my = (int) event.y();
        int button = event.button();

        if (searchBox != null
                && searchBox.isMouseOver(event.x(), event.y())) {
            return super.mouseClicked(event, doubleClick);
        }

        // Category selection
        if (button == 0) {
            for (int i = 0; i < CATEGORIES.length; i++) {
                int bx = px + 13;
                int by = py + 126 + i * 39;

                if (hit(mx, my, bx, by, sidebar - 26, 29)) {
                    selected = i;
                    scrollOffset = 0;
                    settingsFeature = null;

                    if (searchBox != null) {
                        searchBox.setValue("");
                    }

                    return true;
                }
            }
        }

        // Settings popup
        if (settingsFeature != null) {
            int panelW = 190;
            int panelH = 90;
            int x = px + pw - panelW - 14;
            int y = py + ph - panelH - 14;

            if (hit(mx, my, x, y, panelW, panelH)) {
                if (button == 0) {
                    switch (settingsFeature.name) {
                        case "HitSound" ->
                                cfg.hitSoundType =
                                        (cfg.hitSoundType + 1) % 7;

                        case "CriticalEffects" ->
                                cfg.criticalStyle =
                                        (cfg.criticalStyle + 1) % 4;

                        case "JumpCircle" ->
                                cfg.jumpCircleThickness =
                                        Mth.clamp(
                                                cfg.jumpCircleThickness + 0.5f,
                                                0.5f, 10.0f
                                        );

                        case "TargetESP" -> {
                            cfg.targetEspRing = !cfg.targetEspRing;
                        }

                        case "NoRender" -> {
                            cfg.hideFire = !cfg.hideFire;
                        }
                    }

                    cfg.save();
                    return true;
                }
            }

            if (button == 1) {
                settingsFeature = null;
                return true;
            }
        }

        List<Feature> visible = filteredFeatures();

        int columns = 2;
        int gap = 9;
        int cardW = (cw - gap) / columns;

        int startY = py + 78;
        int bottom = py + ph - 12;

        int rowsVisible = Math.max(1,
                (bottom - startY + cardGap)
                        / (cardHeight + cardGap));

        int startIndex = scrollOffset * columns;
        int endIndex = Math.min(visible.size(),
                startIndex + rowsVisible * columns);

        for (int i = startIndex; i < endIndex; i++) {
            int local = i - startIndex;
            int col = local % columns;
            int row = local / columns;

            int x = cx + col * (cardW + gap);
            int y = startY + row * (cardHeight + cardGap);

            if (!hit(mx, my, x, y, cardW, cardHeight)) {
                continue;
            }

            Feature feature = visible.get(i);

            int gearX = x + cardW - 29;
            int gearY = y + 7;

            boolean gearClicked = hit(
                    mx, my, gearX, gearY, 21, 21
            );

            if (button == 1 || gearClicked) {
                settingsFeature =
                        settingsFeature == feature ? null : feature;

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
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double deltaX, double deltaY) {
        if (hit((int) mouseX, (int) mouseY,
                cx, py + 70, cw, ph - 80)) {
            scrollOffset = Math.max(0,
                    scrollOffset + (deltaY < 0 ? 1 : -1));

            return true;
        }

        return super.mouseScrolled(
                mouseX, mouseY, deltaX, deltaY
        );
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
