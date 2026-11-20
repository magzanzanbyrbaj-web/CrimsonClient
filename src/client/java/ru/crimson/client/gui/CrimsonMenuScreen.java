
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

    public CrimsonMenuScreen() {
        super(Component.literal("Crimson Client"));
        cfg = CrimsonClientClient.config;
        buildFeatures();
    }

    private void buildFeatures() {
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

        add("Smooth Sword", "Sword animation",
                () -> cfg.smoothSword, v -> cfg.smoothSword = v, 3);
        add("Item Switch", "Item switch animation",
                () -> cfg.itemSwitchAnim, v -> cfg.itemSwitchAnim = v, 3);
        add("Totem Animation", "Totem animation",
                () -> cfg.totemAnimation, v -> cfg.totemAnimation = v, 3);
        add("Custom Crosshair", "Custom crosshair",
                () -> cfg.customCrosshair, v -> cfg.customCrosshair = v, 3);
        add("Custom Sky", "Sky visuals",
                () -> cfg.customSky, v -> cfg.customSky = v, 3);
        add("Inventory Blur", "Inventory background effect",
                () -> cfg.inventoryBlur, v -> cfg.inventoryBlur = v, 3);

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
        pw = Math.min(540, width - 20);
        ph = Math.min(340, height - 20);
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

        text(g, "Visual modules and settings",
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

            rounded(g, x, cy, cardW, cardH, 8,
                    alpha(enabled ? 0x883C202D : 0x66343A4B));
            rounded(g, x, cy, cardW, 1, 1,
                    alpha(enabled ? 0xCCFF5365 : 0x55727A8C));

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

            if (hover) {
                rounded(g, x + 5, cy + cardH - 2,
                        cardW - 10, 1, 1, alpha(RED));
            }
        }
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

        if (event.button() == 0) {
            int mx = (int) event.x();
            int my = (int) event.y();

            for (int i = 0; i < CATEGORIES.length; i++) {
                if (hit(mx, my, px + 8, py + 57 + i * 34,
                        sidebar - 16, 28)) {
                    selected = i;
                    if (searchBox != null) searchBox.setValue("");
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
                    f.set.accept(!f.get.getAsBoolean());
                    cfg.save();
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

    private record Feature(String name, String desc,
                           BooleanSupplier get, Consumer<Boolean> set,
                           int category) {}
}
