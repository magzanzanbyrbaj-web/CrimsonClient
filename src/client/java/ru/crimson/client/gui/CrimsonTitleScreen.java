
package ru.crimson.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class CrimsonTitleScreen extends Screen {

    private static final int ACCENT = 0xFFFF5268;
    private static final int TEXT = 0xFFF4F3F7;
    private static final int MUTED = 0xFFAAA8B4;

    private static final Identifier FONT =
            Identifier.fromNamespaceAndPath(
                    "crimsonclient", "inter"
            );

    private static final Identifier LOGO =
            Identifier.fromNamespaceAndPath(
                    "crimsonclient", "crimson_logo.png"
            );

    private final List<MenuButton> buttons = new ArrayList<>();
    private long openedAt;

    public CrimsonTitleScreen() {
        super(Component.literal("Crimson Client"));
    }

    private Component styled(String value) {
        return Component.literal(value)
                .setStyle(Style.EMPTY.withFont(
                        new FontDescription.Resource(FONT)
                ));
    }

    private int textWidth(String value) {
        return font.width(styled(value));
    }

    private void drawText(
            GuiGraphics g,
            String value,
            int x,
            int y,
            int color,
            boolean shadow
    ) {
        g.drawString(
                font,
                styled(value),
                x,
                y,
                color,
                shadow
        );
    }

    @Override
    protected void init() {
        openedAt = System.currentTimeMillis();
        buttons.clear();

        int buttonWidth = Math.min(250, Math.max(190, width / 4));
        int buttonHeight = 30;
        int gap = 9;

        int x = (width - buttonWidth) / 2;
        int startY = height / 2 - 8;

        buttons.add(new MenuButton(
                x, startY, buttonWidth, buttonHeight,
                "SINGLEPLAYER",
                () -> Minecraft.getInstance().setScreen(
                        new SelectWorldScreen(this))
        ));

        buttons.add(new MenuButton(
                x, startY + buttonHeight + gap,
                buttonWidth, buttonHeight,
                "MULTIPLAYER",
                () -> Minecraft.getInstance().setScreen(
                        new JoinMultiplayerScreen(this))
        ));

        buttons.add(new MenuButton(
                x, startY + (buttonHeight + gap) * 2,
                buttonWidth, buttonHeight,
                "SETTINGS",
                () -> Minecraft.getInstance().setScreen(
                        new OptionsScreen(
                                this,
                                Minecraft.getInstance().options
                        ))
        ));

        buttons.add(new MenuButton(
                x, startY + (buttonHeight + gap) * 3,
                buttonWidth, buttonHeight,
                "QUIT GAME",
                () -> Minecraft.getInstance().stop()
        ));
    }

    @Override
    public void render(
            GuiGraphics g,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        drawAnimatedBackground(g);
        drawBrand(g);

        for (MenuButton button : buttons) {
            boolean hovered =
                    mouseX >= button.x
                    && mouseX <= button.x + button.width
                    && mouseY >= button.y
                    && mouseY <= button.y + button.height;

            drawButton(g, button, hovered);
        }

        String footer = "CRIMSON CLIENT  •  1.21.11";
        drawText(g, footer, 12, height - 18, 0x887F7D89, false);

        String version = "MINECRAFT 1.21.11";
        drawText(
                g,
                version,
                width - textWidth(version) - 12,
                height - 18,
                0x887F7D89,
                false
        );
    }

    private void drawAnimatedBackground(GuiGraphics g) {
        long now = System.currentTimeMillis();
        float t = (now - openedAt) / 1000.0f;

        g.fill(0, 0, width, height, 0xFF080A12);

        for (int i = 0; i < 8; i++) {
            float phase = t * 0.18f + i * 0.82f;

            int centerX = (int) (
                    width * (
                            0.18 + 0.68 *
                            (0.5 + 0.5 * Math.sin(phase))
                    )
            );

            int centerY = (int) (
                    height * (
                            0.28 + 0.40 *
                            (0.5 + 0.5 * Math.cos(phase * 0.73))
                    )
            );

            int radiusX = Math.max(90, width / 5 + i * 13);
            int radiusY = Math.max(55, height / 7 + i * 7);
            int alpha = 7 + (i % 3) * 3;

            int color = (alpha << 24)
                    | (i % 2 == 0 ? 0x5B1425 : 0x251A42);

            g.fill(
                    centerX - radiusX,
                    centerY - radiusY,
                    centerX + radiusX,
                    centerY + radiusY,
                    color
            );
        }

        for (int i = 0; i < 72; i++) {
            double seed = i * 127.1;

            float sx = (float) (
                    (Math.sin(seed) * 0.5 + 0.5) * width
            );

            float sy = (float) (
                    (Math.cos(seed * 0.73) * 0.5 + 0.5) * height
            );

            float drift = (
                    t * (3.0f + (i % 5)) + i * 17
            ) % (height + 20);

            int x = (int) sx;
            int y = (int) ((sy + drift) % height);

            int twinkle = (int) (
                    100 + 90 *
                    (0.5 + 0.5 * Math.sin(t * 1.7 + i))
            );

            int color = (twinkle << 24) | 0x00DDE4F5;
            int size = i % 9 == 0 ? 2 : 1;

            g.fill(x, y, x + size, y + size, color);
        }

        g.fill(0, 0, width, 34, 0x55000000);
        g.fill(0, height - 46, width, height, 0x66000000);
    }

    private void drawBrand(GuiGraphics g) {
        int centerX = width / 2;

        // Client logo
        int logoSize = 76;
        int logoX = centerX - logoSize / 2;
        int logoY = height / 4 - 78;

        g.blit(
                RenderPipelines.GUI_TEXTURED,
                LOGO,
                logoX,
                logoY,
                0f,
                0f,
                logoSize,
                logoSize,
                128,
                128
        );

        String title = "CRIMSON";
        String subtitle = "CLIENT";
        String slogan = "A cleaner way to play";

        drawText(
                g,
                title,
                centerX - textWidth(title) / 2,
                height / 4 + 4,
                TEXT,
                true
        );

        g.fill(
                centerX - 47,
                height / 4 + 19,
                centerX + 47,
                height / 4 + 21,
                ACCENT
        );

        drawText(
                g,
                subtitle,
                centerX - textWidth(subtitle) / 2,
                height / 4 + 29,
                MUTED,
                false
        );

        drawText(
                g,
                slogan,
                centerX - textWidth(slogan) / 2,
                height / 4 + 44,
                0xFF777581,
                false
        );
    }

    private void drawButton(
            GuiGraphics g,
            MenuButton button,
            boolean hovered
    ) {
        int fill = hovered
                ? 0xE52A202B
                : 0xC9161721;

        int border = hovered
                ? 0xFFFF6578
                : 0x554F505D;

        rounded(
                g,
                button.x,
                button.y,
                button.width,
                button.height,
                7,
                0x55000000
        );

        rounded(
                g,
                button.x,
                button.y,
                button.width,
                button.height,
                7,
                fill
        );

        g.fill(
                button.x + 1,
                button.y + 1,
                button.x + button.width - 1,
                button.y + 2,
                border
        );

        int labelWidth = textWidth(button.label);

        drawText(
                g,
                button.label,
                button.x + (button.width - labelWidth) / 2,
                button.y + (button.height - 8) / 2,
                hovered ? TEXT : 0xFFD5D2DC,
                false
        );

        if (hovered) {
            g.fill(
                    button.x + 9,
                    button.y + 8,
                    button.x + 11,
                    button.y + button.height - 8,
                    ACCENT
            );
        }
    }

    private static void rounded(
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

        for (int row = 0; row < radius; row++) {
            int inset = (int) Math.ceil(
                    radius - Math.sqrt(
                            radius * radius
                            - (radius - row) * (radius - row)
                    )
            );

            g.fill(
                    x + inset,
                    y + row,
                    x + w - inset,
                    y + row + 1,
                    color
            );

            g.fill(
                    x + inset,
                    y + h - row - 1,
                    x + w - inset,
                    y + h - row,
                    color
            );
        }

        g.fill(
                x,
                y + radius,
                x + w,
                y + h - radius,
                color
        );
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (event.button() == 0) {
            int mx = (int) event.x();
            int my = (int) event.y();

            for (MenuButton button : buttons) {
                if (
                        mx >= button.x
                        && mx <= button.x + button.width
                        && my >= button.y
                        && my <= button.y + button.height
                ) {
                    button.action.run();
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            Minecraft.getInstance().stop();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record MenuButton(
            int x,
            int y,
            int width,
            int height,
            String label,
            Runnable action
    ) {}
}
