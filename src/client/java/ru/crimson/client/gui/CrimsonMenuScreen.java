
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

    private static final int CARD_HEIGHT = 49;
    private static final int CARD_GAP = 7;

    private Feature settingsFeature;

    public CrimsonMenuScreen() {
        super(Component.literal("Crimson Client"));
        cfg = CrimsonClientClient.config;
        buildFeatures();
    }

    private void buildFeatures() {
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
        add("ChinaHat", "Animated hat above your player",
                () -> cfg.chinaHat, v -> cfg.chinaHat = v, 0);
        add("Trails", "Player movement trail",
                () -> cfg.trails, v -> cfg.trails = v, 0);
        add("ChatAnimations", "Smooth chat animations",
                () -> cfg.chatAnimations, v -> cfg.chatAnimations = v, 0);
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
        add("ReachDisplay", "Distance to selected entity",
                () -> cfg.reachDisplay, v -> cfg.reachDisplay = v, 0);
        add("PearlTracker", "Ender pearl tracking",
                () -> cfg.pearlTracker, v -> cfg.pearlTracker = v, 0);

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

        add("Watermark", "Client name, FPS and ping",
                () -> cfg.watermark, v -> cfg.watermark = v, 2);
        add("Keystrokes", "Keyboard display",
                () -> cfg.keystrokes, v -> cfg.keystrokes = v, 2);
        add("ModuleList", "Enabled module list",
                () -> cfg.moduleList, v -> cfg.moduleList = v, 2);

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
        pw = Math.min(width - 24, Math.max(500, (int)(width * 0.72f)));
        ph = Math.min(height - 24, Math.max(340, (int)(height * 0.72f)));

        px = (width - pw) / 2;
        py = (height - ph) / 2;

        sidebar = Math.max(112, (int)(pw * 0.17f));
        cx = px + sidebar + 14;
        cw = pw - sidebar - 28;
    }

    @Override
    protected void init() {
        layout();

        int searchWidth = Math.min(190, Math.max(110, cw / 3));

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

    private void text(GuiGraphics g, String value, int x, int y, int color) {
        Component component = Component.literal(value)
                .setStyle(Style.EMPTY.withFont(new FontDescription.Resource(FONT)));
        g.drawString(font, component, x, y, color, false);
    }

    private static void rounded(GuiGraphics g, int x, int y,
                                int w, int h, int radius, int color) {
        if (w <= 0 || h <= 0) return;

        radius = Math.max(1, Math.min(radius, Math.min(w / 2, h / 2)));

        for (int row = 0; row < radius; row++) {
            int inset = (int)Math.ceil(radius - Math.sqrt(
                    radius * radius - (radius - row) * (radius - row)));

            g.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
            g.fill(x + inset, y + h - row - 1,
                    x + w - inset, y + h - row, color);
        }

        g.fill(x, y + radius, x + w, y + h - radius, color);
    }

    private boolean hit(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private List<Feature> filteredFeatures() {
        List<Feature> result = new ArrayList<>();

        String query = searchBox == null ? "" :
                searchBox.getValue().trim().toLowerCase(Locale.ROOT);

        for (Feature feature : features) {
            if (query.isEmpty()) {
                if (feature.category == selected) result.add(feature);
            } else if (feature.name.toLowerCase(Locale.ROOT).contains(query)
                    || feature.desc.toLowerCase(Locale.ROOT).contains(query)) {
                result.add(feature);
            }
        }

        return result;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        layout();

        g.fill(0, 0, width, height, 0x66000000);

        rounded(g, px + 3, py + 5, pw, ph, 16, 0x66000000);
        rounded(g, px, py, pw, ph, 16, 0xF0090A0C);
        rounded(g, px + 1, py + 1, pw - 2, ph - 2, 15, 0xF20D0E10);

        rounded(g, px + 1, py + 1, sidebar, ph - 2, 14, 0xFF0B0C0E);
        g.fill(px + sidebar, py + 13, px + sidebar + 1, py + ph - 13, 0x332F3035);

        drawSidebar(g, mouseX, mouseY);
        drawHeader(g);
        drawCards(g, mouseX, mouseY);
        drawSettings(g);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawSidebar(GuiGraphics g, int mx, int my) {
        text(g, "CRIMSON", px + 18, py + 23, RED);
        text(g, "CLIENT", px + 18, py + 37, MUTED);

        for (int i = 0; i < CATEGORIES.length; i++) {
            int bx = px + 13;
            int by = py + 105 + i * 37;

            boolean active = selected == i;
            boolean hover = hit(mx, my, bx, by, sidebar - 26, 29);

            if (active) {
                rounded(g, bx, by, sidebar - 26, 29, 7, 0xFF252027);
                rounded(g, bx, by + 4, 2, 21, 1, RED);
            } else if (hover) {
                rounded(g, bx, by, sidebar - 26, 29, 7, 0xFF15161A);
            }

            text(g, CATEGORIES[i], bx + 11, by + 10, active ? WHITE : MUTED);
        }

        int profileY = py + ph - 47;
        rounded(g, px + 12, profileY, sidebar - 26, 31, 8, 0xFF141519);
        rounded(g, px + 18, profileY + 5, 21, 21, 7, 0xFF393A40);

        Minecraft mc = Minecraft.getInstance();
        String player = mc.player == null ? "Player" : mc.player.getName().getString();
        if (player.length() > 12) player = player.substring(0, 12);

        text(g, player, px + 44, profileY + 7, WHITE);
        text(g, "Crimson user", px + 44, profileY + 19, MUTED);
    }

    private void drawHeader(GuiGraphics g) {
        int searchWidth = Math.min(190, Math.max(110, cw / 3));
        int titleWidth = cw - searchWidth - 10;

        rounded(g, cx, py + 15, titleWidth, 27, 8, 0xFF111216);
        text(g, "▣", cx + 11, py + 24, 0xFFB9BAC0);
        text(g, CATEGORIES[selected], cx + 31, py + 24, WHITE);

        text(g, "VISUAL MODULES",
                cx + titleWidth - 115, py + 24, MUTED);

        int searchX = px + pw - searchWidth - 15;
        rounded(g, searchX, py + 15, searchWidth, 27, 8, 0xFF111216);
        text(g, "⌕", searchX + searchWidth - 20, py + 23, MUTED);
    }

    private void drawCards(GuiGraphics g, int mx, int my) {
        List<Feature> visible = filteredFeatures();

        int columns = settingsFeature == null ? 2 : 2;
        int gap = 8;
        int cardW = (cw - gap) / columns;

        int startY = py + 57;
        int bottom = py + ph - 12;
        int rowsVisible = Math.max(1,
                (bottom - startY + CARD_GAP) / (CARD_HEIGHT + CARD_GAP));

        int maxOffset = Math.max(0,
                (int)Math.ceil((double)visible.size() / columns) - rowsVisible);

        scrollOffset = Mth.clamp(scrollOffset, 0, maxOffset);

        int startIndex = scrollOffset * columns;
        int endIndex = Math.min(visible.size(), startIndex + rowsVisible * columns);

        if (visible.isEmpty()) {
            text(g, "Ничего не найдено", cx + 12, startY + 15, MUTED);
            return;
        }

        for (int i = startIndex; i < endIndex; i++) {
            Feature f = visible.get(i);
            int local = i - startIndex;
            int col = local % columns;
            int row = local / columns;

            int x = cx + col * (cardW + gap);
            int y = startY + row * (CARD_HEIGHT + CARD_GAP);

            boolean enabled = f.get.getAsBoolean();
            boolean hover = hit(mx, my, x, y, cardW, CARD_HEIGHT);
            boolean chosen = settingsFeature == f;

            rounded(g, x, y, cardW, CARD_HEIGHT, 8,
                    hover ? 0xFF191A1F : 0xFF111216);

            g.fill(x, y, x + cardW, y + 1, chosen ? RED : 0xFF202126);

            text(g, f.name, x + 11, y + 11, enabled ? WHITE : 0xFF9B9CA3);

            String desc = f.desc;
            while (!desc.isEmpty() && font.width(Component.literal(desc)) > cardW - 42) {
                desc = desc.substring(0, desc.length() - 1);
            }

            text(g, desc, x + 11, y + 29, 0xFF65666D);

            int gearX = x + cardW - 25;
            rounded(g, gearX, y + 8, 16, 16, 5,
                    chosen ? 0xFF393A40 : 0xFF1A1B20);
            text(g, "⚙", gearX + 3, y + 11, 0xFF96979E);

            if (enabled) {
                rounded(g, x + cardW - 8, y + 2, 3, 3, 2, RED);
            }
        }
    }

    private void drawSettings(GuiGraphics g) {
        if (settingsFeature == null) return;

        int panelW = Math.min(205, cw - 8);
        int panelH = 112;

        int x = px + pw - panelW - 13;
        int y = py + ph - panelH - 13;

        rounded(g, x + 2, y + 3, panelW, panelH, 9, 0x77000000);
        rounded(g, x, y, panelW, panelH, 9, 0xFF17181D);

        g.fill(x + 10, y + 28, x + panelW - 10, y + 29, 0xFF303137);

        text(g, settingsFeature.name, x + 11, y + 12, WHITE);
        text(g, "MODULE SETTINGS", x + 11, y + 39, MUTED);

        text(g, settingsInfo(), x + 11, y + 58, 0xFFD0D0D4);
        text(g, "ЛКМ — изменить", x + 11, y + 80, MUTED);
        text(g, "ПКМ — закрыть", x + 11, y + 95, MUTED);
    }

    private String settingsInfo() {
        return switch (settingsFeature.name) {
            case "HitSound" -> "Sound: " + hitSoundName();
            case "CriticalEffects" -> "Style: " + criticalName();
            case "JumpCircle" -> "Thickness: " + cfg.jumpCircleThickness;
            case "TargetESP" -> "Ring: " + (cfg.targetEspRing ? "ON" : "OFF");
            case "ChinaHat" -> "Rainbow: " + (cfg.chinaHatRainbow ? "ON" : "OFF");
            case "Trails" -> "Length: " + cfg.trailsLength;
            case "NoRender" -> "Visual effects options";
            default -> "Дополнительные настройки";
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

    private void changeSetting() {
        switch (settingsFeature.name) {
            case "HitSound" -> cfg.hitSoundType = (cfg.hitSoundType + 1) % 7;
            case "CriticalEffects" -> cfg.criticalStyle = (cfg.criticalStyle + 1) % 4;
            case "JumpCircle" -> cfg.jumpCircleThickness =
                    Mth.clamp(cfg.jumpCircleThickness + 0.5f, 0.5f, 10.0f);
            case "TargetESP" -> cfg.targetEspRing = !cfg.targetEspRing;
            case "ChinaHat" -> cfg.chinaHatRainbow = !cfg.chinaHatRainbow;
            case "Trails" -> {
                cfg.trailsLength += 10;
                if (cfg.trailsLength > 100) cfg.trailsLength = 20;
            }
            case "NoRender" -> cfg.hideFire = !cfg.hideFire;
        }

        cfg.save();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mx = (int)event.x();
        int my = (int)event.y();
        int button = event.button();

        if (searchBox != null && searchBox.isMouseOver(event.x(), event.y())) {
            return super.mouseClicked(event, doubleClick);
        }

        if (button == 0) {
            for (int i = 0; i < CATEGORIES.length; i++) {
                int bx = px + 13;
                int by = py + 105 + i * 37;

                if (hit(mx, my, bx, by, sidebar - 26, 29)) {
                    selected = i;
                    scrollOffset = 0;
                    settingsFeature = null;
                    if (searchBox != null) searchBox.setValue("");
                    return true;
                }
            }
        }

        if (settingsFeature != null) {
            int panelW = Math.min(205, cw - 8);
            int panelH = 112;
            int x = px + pw - panelW - 13;
            int y = py + ph - panelH - 13;

            if (hit(mx, my, x, y, panelW, panelH)) {
                if (button == 0) {
                    changeSetting();
                    return true;
                }
                if (button == 1) {
                    settingsFeature = null;
                    return true;
                }
            }
        }

        List<Feature> visible = filteredFeatures();
        int columns = 2;
        int gap = 8;
        int cardW = (cw - gap) / columns;
        int startY = py + 57;
        int bottom = py + ph - 12;
        int rowsVisible = Math.max(1,
                (bottom - startY + CARD_GAP) / (CARD_HEIGHT + CARD_GAP));

        int startIndex = scrollOffset * columns;
        int endIndex = Math.min(visible.size(), startIndex + rowsVisible * columns);

        for (int i = startIndex; i < endIndex; i++) {
            int local = i - startIndex;
            int col = local % columns;
            int row = local / columns;

            int x = cx + col * (cardW + gap);
            int y = startY + row * (CARD_HEIGHT + CARD_GAP);

            if (!hit(mx, my, x, y, cardW, CARD_HEIGHT)) continue;

            Feature feature = visible.get(i);
            int gearX = x + cardW - 28;
            boolean gearClicked = hit(mx, my, gearX, y + 5, 23, 23);

            if (button == 1 || gearClicked) {
                settingsFeature = settingsFeature == feature ? null : feature;
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
        if (hit((int)mouseX, (int)mouseY,
                cx, py + 50, cw, ph - 60)) {
            scrollOffset = Math.max(0,
                    scrollOffset + (deltaY < 0 ? 1 : -1));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
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
