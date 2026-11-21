
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

import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.ConfigManager;

import java.util.List;

public class ConfigsScreen extends Screen {

    private static final Identifier FONT =
            Identifier.fromNamespaceAndPath("crimsonclient", "inter");

    private final Screen parent;

    private EditBox nameBox;
    private List<String> configs = List.of();

    private int selected = -1;
    private int panelX, panelY, panelW, panelH;

    private String status = "";

    private static final int RED = 0xFFE64B5D;
    private static final int WHITE = 0xFFF0F0F2;
    private static final int MUTED = 0xFF85868E;

    public ConfigsScreen(Screen parent) {
        super(Component.literal("Crimson Configs"));
        this.parent = parent;
    }

    private void drawText(
            GuiGraphics g,
            String value,
            int x,
            int y,
            int color
    ) {
        Component component = Component.literal(value)
                .setStyle(Style.EMPTY.withFont(
                        new FontDescription.Resource(FONT)
                ));

        g.drawString(font, component, x, y, color, false);
    }

    @Override
    protected void init() {
        panelW = Math.min(360, width - 24);
        panelH = Math.min(290, height - 24);

        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        nameBox = new EditBox(
                font,
                panelX + 16,
                panelY + 52,
                panelW - 32,
                22,
                Component.literal("Config name")
        );

        nameBox.setHint(Component.literal("Название конфига"));
        nameBox.setMaxLength(32);
        nameBox.setBordered(false);
        nameBox.setTextColor(WHITE);

        addRenderableWidget(nameBox);

        refresh();
    }

    private void refresh() {
        configs = ConfigManager.list();

        if (selected >= configs.size()) {
            selected = -1;
        }
    }

    private void drawButton(
            GuiGraphics g,
            String label,
            int x,
            int y,
            int w,
            int h,
            int color
    ) {
        g.fill(x, y, x + w, y + h, color);
        drawText(g, label, x + 8, y + 8, WHITE);
    }

    private boolean inside(
            int mx,
            int my,
            int x,
            int y,
            int w,
            int h
    ) {
        return mx >= x && mx <= x + w
                && my >= y && my <= y + h;
    }

    @Override
    public void render(
            GuiGraphics g,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        g.fill(0, 0, width, height, 0x99000000);

        g.fill(
                panelX,
                panelY,
                panelX + panelW,
                panelY + panelH,
                0xFF101115
        );

        g.fill(
                panelX,
                panelY,
                panelX + panelW,
                panelY + 2,
                RED
        );

        drawText(
                g,
                "CRIMSON CONFIGS",
                panelX + 16,
                panelY + 16,
                RED
        );

        drawText(
                g,
                "Управление конфигурациями",
                panelX + 16,
                panelY + 33,
                MUTED
        );

        int listY = panelY + 84;
        int listBottom = panelY + panelH - 86;

        if (configs.isEmpty()) {
            drawText(
                    g,
                    "Нет сохранённых конфигов",
                    panelX + 16,
                    listY + 5,
                    MUTED
            );
        }

        for (int i = 0; i < configs.size(); i++) {
            int y = listY + i * 22;

            if (y + 19 > listBottom) {
                break;
            }

            boolean active = selected == i;

            g.fill(
                    panelX + 13,
                    y,
                    panelX + panelW - 13,
                    y + 19,
                    active ? 0xFF352027 : 0xFF191A1F
            );

            drawText(
                    g,
                    configs.get(i),
                    panelX + 21,
                    y + 5,
                    active ? RED : WHITE
            );
        }

        int buttonY = panelY + panelH - 56;
        int buttonW = (panelW - 42) / 3;

        drawButton(
                g, "Сохранить",
                panelX + 13, buttonY,
                buttonW, 23, 0xFF252027
        );

        drawButton(
                g, "Загрузить",
                panelX + 21 + buttonW, buttonY,
                buttonW, 23, 0xFF252027
        );

        drawButton(
                g, "Удалить",
                panelX + 29 + buttonW * 2, buttonY,
                buttonW, 23, 0xFF252027
        );

        if (!status.isEmpty()) {
            drawText(
                    g,
                    status,
                    panelX + 16,
                    panelY + panelH - 25,
                    RED
            );
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        int mx = (int) event.x();
        int my = (int) event.y();

        if (nameBox != null
                && nameBox.isMouseOver(event.x(), event.y())) {
            return super.mouseClicked(event, doubleClick);
        }

        int listY = panelY + 84;
        int listBottom = panelY + panelH - 86;

        for (int i = 0; i < configs.size(); i++) {
            int y = listY + i * 22;

            if (y + 19 > listBottom) {
                break;
            }

            if (inside(
                    mx, my,
                    panelX + 13, y,
                    panelW - 26, 19
            )) {
                selected = i;
                status = "";
                return true;
            }
        }

        int buttonY = panelY + panelH - 56;
        int buttonW = (panelW - 42) / 3;

        if (my >= buttonY && my <= buttonY + 23) {

            if (inside(
                    mx, my,
                    panelX + 13, buttonY,
                    buttonW, 23
            )) {
                String name = nameBox.getValue().trim();

                if (name.isEmpty()) {
                    status = "Введите название";
                    return true;
                }

                if (CrimsonClientClient.config == null) {
                    status = "Конфигурация не загружена";
                    return true;
                }

                boolean saved = ConfigManager.save(
                        name,
                        CrimsonClientClient.config
                );

                status = saved
                        ? "Конфиг сохранён"
                        : "Ошибка сохранения";

                refresh();
                return true;
            }

            if (inside(
                    mx, my,
                    panelX + 21 + buttonW,
                    buttonY,
                    buttonW, 23
            )) {
                if (selected < 0 || selected >= configs.size()) {
                    status = "Выбери конфиг";
                    return true;
                }

                boolean loaded = ConfigManager.load(
                        configs.get(selected)
                );

                status = loaded
                        ? "Конфиг загружен"
                        : "Ошибка загрузки";

                refresh();
                return true;
            }

            if (inside(
                    mx, my,
                    panelX + 29 + buttonW * 2,
                    buttonY,
                    buttonW, 23
            )) {
                if (selected < 0 || selected >= configs.size()) {
                    status = "Выбери конфиг";
                    return true;
                }

                boolean deleted = ConfigManager.delete(
                        configs.get(selected)
                );

                status = deleted
                        ? "Конфиг удалён"
                        : "Ошибка удаления";

                selected = -1;
                refresh();
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
