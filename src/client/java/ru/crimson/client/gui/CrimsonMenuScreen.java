
package ru.crimson.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CrimsonMenuScreen extends Screen {
    private static final Identifier LOGO =
            Identifier.fromNamespaceAndPath("crimsonclient", "textures/gui/crimson_logo.png");

    private final long openTime;
    private float animProgress = 0f;
    private final List<GlassButton> buttons = new ArrayList<>();
    private final CrimsonConfig config;
    private int currentTab = 0;

    private static final String[] TABS = {"HUD", "Visuals", "Utilities"};
    private static final int ACCENT = 0xFFFF3434;
    private static final int TEXT_MAIN = 0xFFFFEEEE;
    private static final int TEXT_DIM = 0xFFBFAAAA;
    private static final int TOGGLE_ON = 0xFFFF6666;
    private static final int TOGGLE_OFF = 0xFF777070;

    public CrimsonMenuScreen() {
        super(Component.literal("Crimson Client"));
        this.openTime = System.currentTimeMillis();
        this.config = CrimsonClientClient.config;
        playOpenSound();
    }

    private void playOpenSound() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.2f, 0.7f));
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f));
    }

    @Override
    protected void init() {
        buttons.clear();

        int centerX = width / 2;
        int panelW = Math.min(370, width - 24);
        int panelX = centerX - panelW / 2;
        int panelY = Math.max(8, height / 2 - 180);
        int tabW = Math.min(100, (panelW - 36) / 3);

        for (int i = 0; i < TABS.length; i++) {
            final int tab = i;
            buttons.add(new GlassButton(
                    centerX - (TABS.length * tabW) / 2 + i * tabW,
                    panelY + 67, tabW - 5, 22, TABS[i], currentTab == i,
                    () -> {
                        currentTab = tab;
                        playClickSound();
                        init();
                    }, true));
        }

        List<ToggleEntry> entries = new ArrayList<>();

        if (currentTab == 0) {
            entries.add(new ToggleEntry("Watermark", () -> config.watermark, v -> config.watermark = v));
            entries.add(new ToggleEntry("Target HUD", () -> config.targetHud, v -> config.targetHud = v));
            entries.add(new ToggleEntry("Armor HUD", () -> config.armorHud, v -> config.armorHud = v));
            entries.add(new ToggleEntry("Totem Counter", () -> config.totemCounter, v -> config.totemCounter = v));
            entries.add(new ToggleEntry("Keystrokes", () -> config.keystrokes, v -> config.keystrokes = v));
            entries.add(new ToggleEntry("Module List", () -> config.moduleList, v -> config.moduleList = v));
            entries.add(new ToggleEntry("Inventory HUD", () -> config.inventoryHud, v -> config.inventoryHud = v));
            entries.add(new ToggleEntry("Potion HUD", () -> config.potionHud, v -> config.potionHud = v));
            entries.add(new ToggleEntry("Durability HUD", () -> config.durabilityHud, v -> config.durabilityHud = v));
        } else if (currentTab == 1) {
            entries.add(new ToggleEntry("Hit Particles", () -> config.hitParticles, v -> config.hitParticles = v));
            entries.add(new ToggleEntry("Damage Numbers", () -> config.damageNumbers, v -> config.damageNumbers = v));
            entries.add(new ToggleEntry("Hitmarker", () -> config.hitmarker, v -> config.hitmarker = v));
            entries.add(new ToggleEntry("Critical Effects", () -> config.criticalEffects, v -> config.criticalEffects = v));
            entries.add(new ToggleEntry("Smooth Sword", () -> config.smoothSword, v -> config.smoothSword = v));
            entries.add(new ToggleEntry("Item Switch Anim", () -> config.itemSwitchAnim, v -> config.itemSwitchAnim = v));
            entries.add(new ToggleEntry("Totem Animation", () -> config.totemAnimation, v -> config.totemAnimation = v));
            entries.add(new ToggleEntry("Custom Crosshair", () -> config.customCrosshair, v -> config.customCrosshair = v));
            entries.add(new ToggleEntry("Custom Sky", () -> config.customSky, v -> config.customSky = v));
            entries.add(new ToggleEntry("Inventory Blur", () -> config.inventoryBlur, v -> config.inventoryBlur = v));
        } else {
            entries.add(new ToggleEntry("Fullbright", () -> config.fullbright, v -> config.fullbright = v));
            entries.add(new ToggleEntry("Reduce Particles", () -> config.reduceParticles, v -> config.reduceParticles = v));
            entries.add(new ToggleEntry("Optimization Mode", () -> config.optimized, v -> config.optimized = v));
            entries.add(new ToggleEntry("Menu Animation", () -> config.glassAnimation, v -> config.glassAnimation = v));
        }

        int btnW = panelW - 38;
        int y = panelY + 99;
        int btnH = 21;
        int gap = 4;

        for (ToggleEntry entry : entries) {
            buttons.add(new GlassButton(panelX + 19, y, btnW, btnH, entry.name,
                    entry.getter.getAsBoolean(), () -> {
                entry.setter.accept(!entry.getter.getAsBoolean());
                config.save();
                playClickSound();
            }));
            y += btnH + gap;
        }

        buttons.add(new GlassButton(centerX - 48, panelY + 337, 96, 23, "CLOSE", false, () -> {
            playClickSound();
            onClose();
        }, true));
    }

    private static class ToggleEntry {
        final String name;
        final BooleanSupplier getter;
        final Consumer<Boolean> setter;

        ToggleEntry(String name, BooleanSupplier getter, Consumer<Boolean> setter) {
            this.name = name;
            this.getter = getter;
            this.setter = setter;
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0x99000000);

        float t = (System.currentTimeMillis() - openTime) / 360f;
        animProgress = config.glassAnimation ? Mth.clamp(t, 0f, 1f) : 1f;
        float alpha = easeOutCubic(animProgress);

        int centerX = width / 2;
        int panelW = Math.min(370, width - 24);
        int panelH = 370;
        int panelX = centerX - panelW / 2;
        int panelY = Math.max(8, height / 2 - panelH / 2);

        roundedRect(g, panelX + 3, panelY + 5, panelW, panelH, 15,
                withAlpha(0x99000000, alpha));
        roundedRect(g, panelX, panelY, panelW, panelH, 15,
                withAlpha(0xD9161012, alpha));
        roundedRect(g, panelX + 1, panelY + 1, panelW - 2, panelH - 2, 14,
                withAlpha(0xA52A171B, alpha));
        roundedRect(g, panelX + 10, panelY + 1, panelW - 20, 2, 1,
                withAlpha(ACCENT, alpha));

        g.fill(panelX + 16, panelY + 43, panelX + panelW - 16, panelY + 44,
                withAlpha(0x55FFFFFF, alpha));
        g.fill(panelX + 14, panelY + 16, panelX + 15, panelY + panelH - 16,
                withAlpha(0x55FF7777, alpha));
        g.fill(panelX + panelW - 15, panelY + 16, panelX + panelW - 14, panelY + panelH - 16,
                withAlpha(0x55FF7777, alpha));

        int logoSize = 38;
        g.blit(LOGO, centerX - 88, panelY + 8, 0, 0, logoSize, logoSize, 256, 256);

        g.drawString(font, "CRIMSON", centerX - 42, panelY + 14,
                withAlpha(TEXT_MAIN, alpha), true);
        g.drawString(font, "CLIENT  •  VISUAL EXPERIENCE", centerX - 42, panelY + 27,
                withAlpha(TEXT_DIM, alpha), false);

        for (GlassButton btn : buttons) {
            btn.render(g, mouseX, mouseY, alpha, font);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private static void roundedRect(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;

        int radius = Math.max(1, Math.min(r, Math.min(w / 2, h / 2)));

        for (int row = 0; row < radius; row++) {
            int inset = (int) Math.ceil(radius - Math.sqrt(
                    radius * radius - (radius - row) * (radius - row)));

            g.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
            g.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, color);
        }

        g.fill(x, y + radius, x + w, y + h - radius, color);
    }

    private float easeOutCubic(float t) {
        return 1f - (float) Math.pow(1 - t, 3);
    }

    private static int withAlpha(int color, float alphaMul) {
        int a = (color >>> 24) & 0xFF;
        return ((int) (a * alphaMul) << 24) | (color & 0x00FFFFFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            for (GlassButton btn : buttons) {
                if (btn.isHovered((int) event.x(), (int) event.y())) {
                    btn.onClick.run();
                    if (!btn.label.equals("CLOSE")) init();
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

    @Override
    public void onClose() {
        config.save();
        super.onClose();
    }

    private static class GlassButton {
        final int x, y, w, h;
        final String label;
        final boolean toggled;
        final boolean tab;
        final Runnable onClick;

        GlassButton(int x, int y, int w, int h, String label, boolean toggled, Runnable onClick) {
            this(x, y, w, h, label, toggled, onClick, false);
        }

        GlassButton(int x, int y, int w, int h, String label, boolean toggled,
                    Runnable onClick, boolean tab) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.label = label;
            this.toggled = toggled;
            this.onClick = onClick;
            this.tab = tab;
        }

        boolean isHovered(int mx, int my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }

        void render(GuiGraphics g, int mx, int my, float alpha,
                    net.minecraft.client.gui.Font font) {
            boolean hover = isHovered(mx, my);

            int bg;
            if (tab) {
                bg = toggled ? 0xD9A51F28 : (hover ? 0xB94A252B : 0x8A24191C);
            } else {
                bg = hover ? 0xD95B2028 : 0x8F24191C;
            }

            int border = hover || (tab && toggled) ? 0xFFFF555F : 0x77D94A52;

            roundedRect(g, x, y, w, h, 7, withAlpha(bg, alpha));
            roundedRect(g, x, y, w, 1, 1, withAlpha(border, alpha));

            int textColor = tab && toggled ? 0xFFFFFFFF : TEXT_MAIN;
            int textX = tab ? x + (w - font.width(label)) / 2 : x + 11;

            g.drawString(font, label, textX, y + (h - 8) / 2,
                    withAlpha(textColor, alpha), false);

            if (!tab) {
                String state = toggled ? "ON" : "OFF";
                int stateCol = toggled ? TOGGLE_ON : TOGGLE_OFF;

                g.drawString(font, state, x + w - font.width(state) - 10,
                        y + (h - 8) / 2, withAlpha(stateCol, alpha), false);

                if (toggled) {
                    roundedRect(g, x + 3, y + 5, 3, h - 10, 2,
                            withAlpha(ACCENT, alpha));
                }
            }
        }
    }
}
