
package ru.crimson.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.ArrayList;
import java.util.List;

public class HudEditorScreen extends Screen {

    private static final int ACCENT = 0xFFFF4358;
    private static final int PANEL = 0xEE11131E;
    private static final int PANEL_LIGHT = 0xFF1B1E2B;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int MUTED = 0xFF9295A5;

    private final CrimsonConfig cfg = CrimsonClientClient.config;

    private final List<HudElement> elements = new ArrayList<>();

    private HudElement selected;
    private HudElement dragging;

    private double dragOffsetX;
    private double dragOffsetY;

    private int leftWidth = 112;
    private int rightWidth = 142;

    private int stageLeft;
    private int stageRight;

    private boolean showGrid = true;

    public HudEditorScreen() {
        super(Component.literal("Crimson HUD Editor"));
        buildElements();
    }

    private void buildElements() {
        elements.clear();

        elements.add(new HudElement(
                "Watermark",
                () -> cfg.watermarkX,
                () -> cfg.watermarkY,
                (x, y) -> {
                    cfg.watermarkX = x;
                    cfg.watermarkY = y;
                },
                () -> cfg.watermark,
                value -> cfg.watermark = value,
                154, 29
        ));

        elements.add(new HudElement(
                "Target HUD",
                () -> cfg.targetHudX,
                () -> cfg.targetHudY,
                (x, y) -> {
                    cfg.targetHudX = x;
                    cfg.targetHudY = y;
                },
                () -> cfg.targetHud,
                value -> cfg.targetHud = value,
                166, 53
        ));

        elements.add(new HudElement(
                "Armor HUD",
                () -> cfg.armorHudX,
                () -> cfg.armorHudY,
                (x, y) -> {
                    cfg.armorHudX = x;
                    cfg.armorHudY = y;
                },
                () -> cfg.armorHud,
                value -> cfg.armorHud = value,
                20, 80
        ));

        elements.add(new HudElement(
                "Totem Counter",
                () -> cfg.totemHudX,
                () -> cfg.totemHudY,
                (x, y) -> {
                    cfg.totemHudX = x;
                    cfg.totemHudY = y;
                },
                () -> cfg.totemCounter,
                value -> cfg.totemCounter = value,
                45, 23
        ));

        elements.add(new HudElement(
                "Keystrokes",
                () -> cfg.keystrokesX,
                () -> cfg.keystrokesY,
                (x, y) -> {
                    cfg.keystrokesX = x;
                    cfg.keystrokesY = y;
                },
                () -> cfg.keystrokes,
                value -> cfg.keystrokes = value,
                58, 57
        ));

        elements.add(new HudElement(
                "Module List",
                () -> cfg.moduleListX,
                () -> cfg.moduleListY,
                (x, y) -> {
                    cfg.moduleListX = x;
                    cfg.moduleListY = y;
                },
                () -> cfg.moduleList,
                value -> cfg.moduleList = value,
                110, 120
        ));

        elements.add(new HudElement(
                "Inventory HUD",
                () -> cfg.inventoryHudX,
                () -> cfg.inventoryHudY,
                (x, y) -> {
                    cfg.inventoryHudX = x;
                    cfg.inventoryHudY = y;
                },
                () -> cfg.inventoryHud,
                value -> cfg.inventoryHud = value,
                196, 64
        ));

        elements.add(new HudElement(
                "Potion HUD",
                () -> cfg.potionHudX,
                () -> cfg.potionHudY,
                (x, y) -> {
                    cfg.potionHudX = x;
                    cfg.potionHudY = y;
                },
                () -> cfg.potionHud,
                value -> cfg.potionHud = value,
                125, 52
        ));

        elements.add(new HudElement(
                "Durability HUD",
                () -> cfg.durabilityHudX,
                () -> cfg.durabilityHudY,
                (x, y) -> {
                    cfg.durabilityHudX = x;
                    cfg.durabilityHudY = y;
                },
                () -> cfg.durabilityHud,
                value -> cfg.durabilityHud = value,
                100, 30
        ));

        if (!elements.isEmpty()) {
            selected = elements.get(0);
        }
    }

    @Override
    protected void init() {
        super.init();

        leftWidth = Math.min(112, width / 3);
        rightWidth = Math.min(142, width / 3);

        stageLeft = leftWidth + 8;
        stageRight = width - rightWidth - 8;
    }

    @Override
    public void render(
            GuiGraphics g,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        g.fill(0, 0, width, height, 0xCC080910);

        drawStage(g);
        drawHeader(g);
        drawElementPreviews(g, mouseX, mouseY);
        drawLeftPanel(g, mouseX, mouseY);
        drawRightPanel(g, mouseX, mouseY);
        drawFooter(g);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawStage(GuiGraphics g) {
        rounded(
                g,
                stageLeft,
                38,
                Math.max(1, stageRight - stageLeft),
                height - 82,
                7,
                0x44202435
        );

        if (!showGrid) return;

        int centerX = (stageLeft + stageRight) / 2;
        int centerY = height / 2;

        for (int x = stageLeft + 10; x < stageRight; x += 20) {
            g.fill(x, 40, x + 1, height - 45, 0x223B4055);
        }

        for (int y = 45; y < height - 45; y += 20) {
            g.fill(stageLeft + 2, y, stageRight - 2, y + 1,
                    0x223B4055);
        }

        g.fill(centerX, 40, centerX + 1, height - 45,
                0x335B6075);

        g.fill(stageLeft + 2, centerY,
                stageRight - 2, centerY + 1,
                0x335B6075);
    }

    private void drawHeader(GuiGraphics g) {
        rounded(g, 8, 7, width - 16, 25, 6, PANEL);

        g.drawString(
                font,
                "CRIMSON",
                17,
                15,
                ACCENT,
                false
        );

        String title = "HUD EDITOR";

        g.drawString(
                font,
                title,
                (width - font.width(title)) / 2,
                15,
                TEXT,
                false
        );

        String resolution = width + "x" + height;

        g.drawString(
                font,
                resolution,
                width - font.width(resolution) - 17,
                15,
                MUTED,
                false
        );
    }

    private void drawElementPreviews(
            GuiGraphics g,
            int mouseX,
            int mouseY
    ) {
        for (HudElement element : elements) {
            int x = displayX(element);
            int y = displayY(element);

            boolean hovered = inside(
                    mouseX, mouseY,
                    x, y,
                    displayWidth(element),
                    displayHeight(element)
            );

            boolean active = element == selected;
            boolean enabled = element.enabled.get();

            int background = active
                    ? 0xEE40202B
                    : hovered
                    ? 0xDD29202A
                    : PANEL;

            if (!enabled) {
                background = 0xAA30303A;
            }

            drawPreview(
                    g,
                    element,
                    x,
                    y,
                    background
            );

            int border = active || hovered
                    ? ACCENT
                    : 0xAA777783;

            drawBorder(
                    g,
                    x,
                    y,
                    displayWidth(element),
                    displayHeight(element),
                    border
            );

            if (active) {
                g.drawString(
                        font,
                        element.name,
                        x,
                        Math.max(36, y - 10),
                        ACCENT,
                        true
                );
            }
        }
    }

    private void drawPreview(
            GuiGraphics g,
            HudElement element,
            int x,
            int y,
            int background
    ) {
        int w = displayWidth(element);
        int h = displayHeight(element);

        rounded(g, x, y, w, h, 5, background);

        switch (element.name) {
            case "Watermark" -> {
                g.fill(x + 5, y + 5, x + 24, y + 24, ACCENT);
                g.drawString(font, "C", x + 11, y + 10,
                        TEXT, true);
                g.drawString(font, "CRIMSON", x + 31, y + 5,
                        TEXT, true);
                g.drawString(font, "120 FPS  45 MS",
                        x + 31, y + 16, MUTED, false);
            }

            case "Target HUD" -> {
                g.drawString(font, "Target Player",
                        x + 9, y + 7, TEXT, true);
                g.drawString(font, "20.0 HP",
                        x + 9, y + 21, 0xFFFF7584, false);
                g.fill(x + 9, y + 39, x + w - 9, y + 44,
                        0xFF35212A);
                g.fill(x + 9, y + 39, x + w - 40, y + 44,
                        ACCENT);
            }

            case "Armor HUD" -> {
                for (int i = 0; i < 4; i++) {
                    rounded(g, x + 2, y + i * 20,
                            16, 17, 3, 0xFF343747);
                }
            }

            case "Totem Counter" -> {
                g.drawString(font, "✦ x2",
                        x + 7, y + 8, 0xFFFFD66B, true);
            }

            case "Keystrokes" -> {
                g.drawString(font, "W", x + 24, y + 5,
                        TEXT, true);
                g.drawString(font, "A S D", x + 7, y + 20,
                        TEXT, true);
                g.drawString(font, "LMB RMB", x + 5, y + 37,
                        MUTED, false);
            }

            case "Module List" -> {
                String[] names = {
                        "KillAura", "Target HUD",
                        "ESP", "Fullbright"
                };

                for (int i = 0; i < names.length; i++) {
                    g.drawString(font, names[i],
                            x + 5, y + 5 + i * 17,
                            ACCENT, false);
                }
            }

            case "Inventory HUD" -> {
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 9; col++) {
                        rounded(
                                g,
                                x + 3 + col * 21,
                                y + 3 + row * 20,
                                19, 18, 3,
                                0xFF343747
                        );
                    }
                }
            }

            case "Potion HUD" -> {
                for (int i = 0; i < 3; i++) {
                    rounded(g, x + 3, y + 3 + i * 16,
                            w - 6, 14, 3, 0xFF263C31);
                }

                g.drawString(font, "Speed II",
                        x + 7, y + 6, 0xFFB7F5C4, false);
            }

            case "Durability HUD" -> {
                g.drawString(font, "Durability",
                        x + 5, y + 4, TEXT, false);
                g.drawString(font, "245 / 300",
                        x + 5, y + 16, MUTED, false);
            }
        }

        if (!element.enabled.get()) {
            g.drawString(font, "OFF",
                    x + 5, y + h - 10, 0xFFFF7585, true);
        }
    }

    private void drawLeftPanel(
            GuiGraphics g,
            int mouseX,
            int mouseY
    ) {
        rounded(g, 7, 38, leftWidth, height - 82, 7, PANEL);

        g.drawString(font, "ELEMENTS",
                15, 48, ACCENT, false);

        int y = 66;

        for (HudElement element : elements) {
            boolean active = element == selected;
            boolean hovered = inside(
                    mouseX, mouseY,
                    12, y - 3,
                    leftWidth - 10, 23
            );

            int color = active
                    ? 0xFF40202B
                    : hovered
                    ? PANEL_LIGHT
                    : 0x00000000;

            rounded(g, 12, y - 3,
                    leftWidth - 10, 23, 4, color);

            g.drawString(
                    font,
                    element.name,
                    17,
                    y + 4,
                    active ? ACCENT : TEXT,
                    false
            );

            String state = element.enabled.get() ? "ON" : "OFF";

            int stateColor = element.enabled.get()
                    ? 0xFF72D69A
                    : 0xFFFF7585;

            g.drawString(
                    font,
                    state,
                    leftWidth - font.width(state) - 3,
                    y + 4,
                    stateColor,
                    false
            );

            y += 25;
        }
    }

    private void drawRightPanel(
            GuiGraphics g,
            int mouseX,
            int mouseY
    ) {
        int x = width - rightWidth - 7;

        rounded(g, x, 38, rightWidth, height - 82,
                7, PANEL);

        g.drawString(font, "SETTINGS",
                x + 9, 48, ACCENT, false);

        if (selected == null) return;

        int y = 70;

        g.drawString(font, selected.name,
                x + 9, y, TEXT, true);

        y += 23;

        g.drawString(font, "Position X",
                x + 9, y, MUTED, false);

        g.drawString(font, String.valueOf(selected.getX()),
                x + 9, y + 11, TEXT, false);

        y += 32;

        g.drawString(font, "Position Y",
                x + 9, y, MUTED, false);

        g.drawString(font, String.valueOf(selected.getY()),
                x + 9, y + 11, TEXT, false);

        y += 35;

        drawButton(
                g, x + 8, y,
                rightWidth - 16, 22,
                selected.enabled.get() ? "DISABLE" : "ENABLE",
                mouseX, mouseY
        );

        y += 29;

        drawButton(
                g, x + 8, y,
                rightWidth - 16, 22,
                "RESET POSITION",
                mouseX, mouseY
        );

        y += 29;

        drawButton(
                g, x + 8, y,
                rightWidth - 16, 22,
                showGrid ? "GRID: ON" : "GRID: OFF",
                mouseX, mouseY
        );

        y += 34;

        g.drawString(font, "Drag element",
                x + 9, y, MUTED, false);

        g.drawString(font, "to move it.",
                x + 9, y + 11, MUTED, false);
    }

    private void drawButton(
            GuiGraphics g,
            int x,
            int y,
            int w,
            int h,
            String label,
            int mouseX,
            int mouseY
    ) {
        boolean hovered = inside(mouseX, mouseY, x, y, w, h);

        rounded(
                g, x, y, w, h,
                4,
                hovered ? 0xFF49303A : PANEL_LIGHT
        );

        int textX = x + (w - font.width(label)) / 2;

        g.drawString(
                font,
                label,
                textX,
                y + 7,
                hovered ? ACCENT : TEXT,
                false
        );
    }

    private void drawFooter(GuiGraphics g) {
        rounded(g, 8, height - 35,
                width - 16, 25, 6, PANEL);

        String text = "DRAG: MOVE   R: RESET   ESC: EXIT";

        g.drawString(
                font,
                text,
                (width - font.width(text)) / 2,
                height - 26,
                MUTED,
                false
        );
    }

    private void drawBorder(
            GuiGraphics g,
            int x,
            int y,
            int w,
            int h,
            int color
    ) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private int displayX(HudElement element) {
        int available = Math.max(1, stageRight - stageLeft);

        return stageLeft + (int) (
                element.getX() * (available / (double) width)
        );
    }

    private int displayY(HudElement element) {
        return 42 + (int) (
                element.getY() * ((height - 88) / (double) height)
        );
    }

    private int displayWidth(HudElement element) {
        return Math.max(
                12,
                (int) (element.width *
                        ((stageRight - stageLeft) / (double) width))
        );
    }

    private int displayHeight(HudElement element) {
        return Math.max(
                12,
                (int) (element.height *
                        ((height - 88) / (double) height))
        );
    }

    private boolean inside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int w,
            int h
    ) {
        return mouseX >= x
                && mouseX <= x + w
                && mouseY >= y
                && mouseY <= y + h;
    }

    private void rounded(
            GuiGraphics g,
            int x,
            int y,
            int w,
            int h,
            int radius,
            int color
    ) {
        if (w <= 0 || h <= 0) return;

        radius = Math.max(
                1,
                Math.min(radius, Math.min(w / 2, h / 2))
        );

        g.fill(x + radius, y, x + w - radius, y + h, color);
        g.fill(x, y + radius, x + w, y + h - radius, color);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        double mx = event.x();
        double my = event.y();

        // Выбор элемента в левом списке.
        int listY = 66;

        for (HudElement element : elements) {
            if (inside(mx, my, 12, listY - 3,
                    leftWidth - 10, 23)) {
                selected = element;
                return true;
            }

            listY += 25;
        }

        // Кнопки панели настроек.
        int panelX = width - rightWidth - 7;
        int buttonX = panelX + 8;
        int buttonW = rightWidth - 16;

        if (selected != null) {
            int buttonY = 93;

            if (inside(mx, my, buttonX, buttonY,
                    buttonW, 22)) {
                selected.enabled.set(!selected.enabled.get());
                save();
                return true;
            }

            buttonY += 29;

            if (inside(mx, my, buttonX, buttonY,
                    buttonW, 22)) {
                resetSelected();
                save();
                return true;
            }

            buttonY += 29;

            if (inside(mx, my, buttonX, buttonY,
                    buttonW, 22)) {
                showGrid = !showGrid;
                return true;
            }
        }

        // Перетаскивание элемента на сцене.
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement element = elements.get(i);

            int x = displayX(element);
            int y = displayY(element);

            if (!inside(mx, my, x, y,
                    displayWidth(element),
                    displayHeight(element))) {
                continue;
            }

            selected = element;
            dragging = element;

            dragOffsetX = mx - x;
            dragOffsetY = my - y;

            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if (dragging != null && button == 0) {
            int displayX = (int) (mouseX - dragOffsetX);
            int displayY = (int) (mouseY - dragOffsetY);

            int available = Math.max(1, stageRight - stageLeft);
            int stageHeight = Math.max(1, height - 88);

            int newX = (int) (
                    (displayX - stageLeft) *
                            (width / (double) available)
            );

            int newY = (int) (
                    (displayY - 42) *
                            (height / (double) stageHeight)
            );

            newX = Math.max(0, Math.min(width - dragging.width, newX));
            newY = Math.max(0, Math.min(height - dragging.height, newY));

            dragging.set.accept(newX, newY);

            return true;
        }

        return super.mouseDragged(
                mouseX,
                mouseY,
                button,
                dragX,
                dragY
        );
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && dragging != null) {
            dragging = null;
            save();
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            resetSelected();
            save();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_G) {
            showGrid = !showGrid;
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void resetSelected() {
        if (selected == null) return;

        int index = elements.indexOf(selected);

        int[][] defaults = {
                {10, 10},
                {10, 80},
                {10, 160},
                {10, 220},
                {10, 280},
                {10, 340},
                {10, 400},
                {10, 460},
                {10, 520}
        };

        if (index >= 0 && index < defaults.length) {
            selected.set.accept(
                    defaults[index][0],
                    defaults[index][1]
            );
        }
    }

    private void save() {
        if (cfg != null) {
            cfg.save();
        }
    }

    @Override
    public void onClose() {
        save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class HudElement {

        final String name;

        final java.util.function.IntSupplier x;
        final java.util.function.IntSupplier y;

        final PositionSetter set;

        final java.util.function.BooleanSupplier enabled;
        final java.util.function.Consumer<Boolean> toggle;

        final int width;
        final int height;

        HudElement(
                String name,
                java.util.function.IntSupplier x,
                java.util.function.IntSupplier y,
                PositionSetter set,
                java.util.function.BooleanSupplier enabled,
                java.util.function.Consumer<Boolean> toggle,
                int width,
                int height
        ) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.set = set;
            this.enabled = enabled;
            this.toggle = toggle;
            this.width = width;
            this.height = height;
        }

        int getX() {
            return x.getAsInt();
        }

        int getY() {
            return y.getAsInt();
        }
    }

    @FunctionalInterface
    private interface PositionSetter {
        void accept(int x, int y);
    }
}
