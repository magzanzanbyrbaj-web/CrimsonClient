
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

    private final Minecraft mc = Minecraft.getInstance();

    private final CrimsonConfig cfg = CrimsonClientClient.config;

    private HudElement dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    private final List<HudElement> elements = new ArrayList<>();

    private static final int ACCENT = 0xFFFF4358;
    private static final int PANEL = 0xDD11131E;
    private static final int TEXT = 0xFFFFFFFF;

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
                154, 29,
                () -> cfg.watermark
        ));

        elements.add(new HudElement(
                "Target HUD",
                () -> cfg.targetHudX,
                () -> cfg.targetHudY,
                (x, y) -> {
                    cfg.targetHudX = x;
                    cfg.targetHudY = y;
                },
                166, 53,
                () -> cfg.targetHud
        ));

        elements.add(new HudElement(
                "Armor HUD",
                () -> cfg.armorHudX,
                () -> cfg.armorHudY,
                (x, y) -> {
                    cfg.armorHudX = x;
                    cfg.armorHudY = y;
                },
                20, 80,
                () -> cfg.armorHud
        ));

        elements.add(new HudElement(
                "Totem Counter",
                () -> cfg.totemHudX,
                () -> cfg.totemHudY,
                (x, y) -> {
                    cfg.totemHudX = x;
                    cfg.totemHudY = y;
                },
                45, 23,
                () -> cfg.totemCounter
        ));

        elements.add(new HudElement(
                "Keystrokes",
                () -> cfg.keystrokesX,
                () -> cfg.keystrokesY,
                (x, y) -> {
                    cfg.keystrokesX = x;
                    cfg.keystrokesY = y;
                },
                58, 57,
                () -> cfg.keystrokes
        ));

        elements.add(new HudElement(
                "Module List",
                () -> cfg.moduleListX,
                () -> cfg.moduleListY,
                (x, y) -> {
                    cfg.moduleListX = x;
                    cfg.moduleListY = y;
                },
                110, 120,
                () -> cfg.moduleList
        ));

        elements.add(new HudElement(
                "Inventory HUD",
                () -> cfg.inventoryHudX,
                () -> cfg.inventoryHudY,
                (x, y) -> {
                    cfg.inventoryHudX = x;
                    cfg.inventoryHudY = y;
                },
                196, 64,
                () -> cfg.inventoryHud
        ));

        elements.add(new HudElement(
                "Potion HUD",
                () -> cfg.potionHudX,
                () -> cfg.potionHudY,
                (x, y) -> {
                    cfg.potionHudX = x;
                    cfg.potionHudY = y;
                },
                125, 52,
                () -> cfg.potionHud
        ));

        elements.add(new HudElement(
                "Durability HUD",
                () -> cfg.durabilityHudX,
                () -> cfg.durabilityHudY,
                (x, y) -> {
                    cfg.durabilityHudX = x;
                    cfg.durabilityHudY = y;
                },
                100, 30,
                () -> cfg.durabilityHud
        ));
    }

    @Override
    public void render(
            GuiGraphics g,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        g.fill(0, 0, width, height, 0x99000000);

        drawHeader(g);

        for (HudElement element : elements) {
            if (!element.enabled.getAsBoolean()) {
                continue;
            }

            drawElement(g, element, mouseX, mouseY);
        }

        drawFooter(g);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawHeader(GuiGraphics g) {
        String title = "CRIMSON HUD EDITOR";
        int titleWidth = font.width(title);

        int x = (width - titleWidth - 30) / 2;

        rounded(
                g,
                x,
                15,
                titleWidth + 30,
                27,
                7,
                PANEL
        );

        g.drawString(
                font,
                title,
                x + 15,
                24,
                ACCENT,
                false
        );
    }

    private void drawFooter(GuiGraphics g) {
        String text = "DRAG - MOVE    R - RESET    ESC - CLOSE";

        int w = font.width(text) + 24;
        int x = (width - w) / 2;
        int y = height - 38;

        rounded(g, x, y, w, 25, 7, PANEL);

        g.drawString(
                font,
                text,
                x + 12,
                y + 9,
                TEXT,
                false
        );
    }

    private void drawElement(
            GuiGraphics g,
            HudElement element,
            int mouseX,
            int mouseY
    ) {
        int x = element.x.getAsInt();
        int y = element.y.getAsInt();

        boolean hovered = inside(
                mouseX,
                mouseY,
                x,
                y,
                element.width,
                element.height
        );

        boolean selected = dragging == element;

        int background = selected
                ? 0xEE401C29
                : hovered
                ? 0xDD29202A
                : PANEL;

        rounded(
                g,
                x,
                y,
                element.width,
                element.height,
                6,
                background
        );

        int border = selected || hovered
                ? ACCENT
                : 0xAA777783;

        g.fill(x, y, x + element.width, y + 1, border);
        g.fill(x, y + element.height - 1,
                x + element.width, y + element.height, border);
        g.fill(x, y, x + 1, y + element.height, border);
        g.fill(x + element.width - 1, y,
                x + element.width, y + element.height, border);

        String name = element.name;

        int textX = x + 5;
        int textY = y + 5;

        if (element.width < font.width(name) + 10) {
            name = name.substring(
                    0,
                    Math.min(name.length(), 3)
            );
        }

        g.drawString(
                font,
                name,
                textX,
                textY,
                TEXT,
                true
        );

        if (element.name.equals("Armor HUD")) {
            for (int i = 0; i < 4; i++) {
                int iy = y + i * 21;

                g.fill(
                        x + 3,
                        iy + 17,
                        x + 17,
                        iy + 18,
                        0xFFFF7585
                );
            }
        }

        if (element.name.equals("Keystrokes")) {
            g.drawString(font, "W", x + 23, y + 20, TEXT, false);
            g.drawString(font, "A S D", x + 8, y + 34, TEXT, false);
        }
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

        g.fill(x + radius, y, x + w - radius, y + h, color);
        g.fill(x, y + radius, x + w, y + h - radius, color);

        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (event.button() == 0) {
            for (int i = elements.size() - 1; i >= 0; i--) {
                HudElement element = elements.get(i);

                if (!element.enabled.getAsBoolean()) {
                    continue;
                }

                int x = element.x.getAsInt();
                int y = element.y.getAsInt();

                if (inside(
                        event.x(),
                        event.y(),
                        x,
                        y,
                        element.width,
                        element.height
                )) {
                    dragging = element;

                    dragOffsetX = event.x() - x;
                    dragOffsetY = event.y() - y;

                    return true;
                }
            }
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
            int newX = (int) (mouseX - dragOffsetX);
            int newY = (int) (mouseY - dragOffsetY);

            newX = Math.max(
                    0,
                    Math.min(width - dragging.width, newX)
            );

            newY = Math.max(
                    0,
                    Math.min(height - dragging.height, newY)
            );

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
    public boolean mouseReleased(
            MouseButtonEvent event
    ) {
        if (event.button() == 0 && dragging != null) {
            dragging = null;

            if (cfg != null) {
                cfg.save();
            }

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
            resetPositions();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void resetPositions() {
        cfg.watermarkX = 10;
        cfg.watermarkY = 10;

        cfg.targetHudX = 10;
        cfg.targetHudY = 80;

        cfg.armorHudX = 10;
        cfg.armorHudY = 160;

        cfg.totemHudX = 10;
        cfg.totemHudY = 220;

        cfg.keystrokesX = 10;
        cfg.keystrokesY = 280;

        cfg.moduleListX = 10;
        cfg.moduleListY = 340;

        cfg.inventoryHudX = 10;
        cfg.inventoryHudY = 400;

        cfg.potionHudX = 10;
        cfg.potionHudY = 460;

        cfg.durabilityHudX = 10;
        cfg.durabilityHudY = 520;

        cfg.save();
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

    private static class HudElement {

        final String name;

        final java.util.function.IntSupplier x;
        final java.util.function.IntSupplier y;

        final PositionSetter set;

        final int width;
        final int height;

        final java.util.function.BooleanSupplier enabled;

        HudElement(
                String name,
                java.util.function.IntSupplier x,
                java.util.function.IntSupplier y,
                PositionSetter set,
                int width,
                int height,
                java.util.function.BooleanSupplier enabled
        ) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.set = set;
            this.width = width;
            this.height = height;
            this.enabled = enabled;
        }
    }

    @FunctionalInterface
    private interface PositionSetter {
        void accept(int x, int y);
    }
}
