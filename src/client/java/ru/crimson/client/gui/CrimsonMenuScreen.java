
package ru.crimson.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CrimsonMenuScreen extends Screen {

    private static final int BG = 0xE9101015;
    private static final int PANEL = 0xFF19191F;
    private static final int ITEM = 0xFF222229;
    private static final int ITEM_HOVER = 0xFF2B2B34;
    private static final int RED = 0xFFE33448;
    private static final int WHITE = 0xFFF3F3F5;
    private static final int MUTED = 0xFF92929D;

    private final List<Feature> features = new ArrayList<>();

    private String category = "Combat";
    private double scroll;
    private int panelX, panelY, panelW, panelH;
    private int mouseX, mouseY;

    public CrimsonMenuScreen() {
        super(Component.literal("Crimson Client"));
        buildFeatures();
    }

    private void buildFeatures() {
        features.clear();

        add("KillAura", "Combat");
        add("AimAssist", "Combat");
        add("TriggerBot", "Combat");
        add("ESP", "Combat");
        add("ChestESP", "Combat");

        add("FreeCamera", "Movement");
        add("Scaffold", "Movement");
        add("Spider", "Movement");
        add("Fly", "Movement");

        add("HitParticles", "Render");
        add("TargetESP", "Render");
        add("CustomCrosshair", "Render");
        add("ChinaHat", "Render");
        add("Trails", "Render");
        add("JumpCircle", "Render");
        add("Fullbright", "Render");

        add("ChestStealer", "Player");
        add("TargetHUD", "Player");
        add("ArmorHUD", "Player");
        add("TotemCounter", "Player");

        add("Watermark", "Misc");
        add("ModuleList", "Misc");
        add("FPSBoost", "Misc");
        add("Keystrokes", "Misc");
    }

    private void add(String name, String category) {
        features.add(new Feature(name, category));
    }

    @Override
    protected void init() {
        panelW = Math.min(650, width - 30);
        panelH = Math.min(390, height - 30);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
    }

    @Override
    public void render(
            GuiGraphics g,
            int mouseX,
            int mouseY,
            float delta
    ) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        g.fill(0, 0, width, height, 0x99000000);
        g.fill(panelX, panelY, panelX + panelW,
                panelY + panelH, BG);

        drawHeader(g);
        drawCategories(g);
        drawFeatures(g);

        super.render(g, mouseX, mouseY, delta);
    }

    private void drawHeader(GuiGraphics g) {
        g.drawString(font, "CRIMSON",
                panelX + 20, panelY + 17, RED, false);

        g.drawString(font, "CLIENT",
                panelX + 84, panelY + 17, WHITE, false);

        g.drawString(font, "1.21.11",
                panelX + panelW - 65,
                panelY + 18, MUTED, false);

        g.fill(panelX + 15, panelY + 38,
                panelX + panelW - 15,
                panelY + 39, 0xFF303039);
    }

    private void drawCategories(GuiGraphics g) {
        String[] categories = {
                "Combat", "Movement", "Render", "Player", "Misc"
        };

        int x = panelX + 17;
        int y = panelY + 52;

        for (String name : categories) {
            int w = 83;
            boolean selected = category.equals(name);
            boolean hover = mouseX >= x && mouseX <= x + w
                    && mouseY >= y && mouseY <= y + 27;

            g.fill(x, y, x + w, y + 27,
                    selected ? 0xFF3A2028 :
                            hover ? ITEM_HOVER : PANEL);

            int color = selected ? RED : WHITE;
            int textW = font.width(name);

            g.drawString(font, name,
                    x + (w - textW) / 2,
                    y + 9, color, false);

            if (selected) {
                g.fill(x + 12, y + 25,
                        x + w - 12, y + 27, RED);
            }

            x += w + 5;
        }
    }

    private void drawFeatures(GuiGraphics g) {
        int startX = panelX + 17;
        int startY = panelY + 94;
        int cardW = (panelW - 45) / 2;
        int cardH = 43;
        int gap = 7;

        int index = 0;

        for (Feature feature : features) {
            if (!feature.category.equals(category)) {
                continue;
            }

            int col = index % 2;
            int row = index / 2;

            int x = startX + col * (cardW + gap);
            int y = startY + row * (cardH + gap)
                    - (int) scroll;

            index++;

            if (y < startY - cardH ||
                    y > panelY + panelH - cardH) {
                continue;
            }

            boolean hover = mouseX >= x && mouseX <= x + cardW
                    && mouseY >= y && mouseY <= y + cardH;

            g.fill(x, y, x + cardW, y + cardH,
                    hover ? ITEM_HOVER : ITEM);

            g.fill(x, y, x + 2, y + cardH,
                    feature.enabled ? RED : 0xFF41414A);

            g.drawString(font, feature.name,
                    x + 12, y + 10, WHITE, false);

            String state = feature.enabled ? "ON" : "OFF";
            int stateColor = feature.enabled ? RED : MUTED;
            int stateW = font.width(state);

            g.drawString(font, state,
                    x + cardW - stateW - 13,
                    y + 15, stateColor, false);
        }
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT
                && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        String[] categories = {
                "Combat", "Movement", "Render", "Player", "Misc"
        };

        int x = panelX + 17;
        int y = panelY + 52;

        for (String name : categories) {
            int w = 83;

            if (mouseX >= x && mouseX <= x + w
                    && mouseY >= y && mouseY <= y + 27) {
                category = name;
                scroll = 0;
                return true;
            }

            x += w + 5;
        }

        int startX = panelX + 17;
        int startY = panelY + 94;
        int cardW = (panelW - 45) / 2;
        int cardH = 43;
        int gap = 7;
        int index = 0;

        for (Feature feature : features) {
            if (!feature.category.equals(category)) {
                continue;
            }

            int col = index % 2;
            int row = index / 2;

            int fx = startX + col * (cardW + gap);
            int fy = startY + row * (cardH + gap)
                    - (int) scroll;

            index++;

            if (mouseX >= fx && mouseX <= fx + cardW
                    && mouseY >= fy && mouseY <= fy + cardH) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    feature.enabled = !feature.enabled;
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontal,
            double vertical
    ) {
        scroll = Math.max(0, Math.min(500,
                scroll - vertical * 20));
        return true;
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class Feature {
        final String name;
        final String category;
        boolean enabled;

        Feature(String name, String category) {
            this.name = name;
            this.category = category;
        }
    }
}
