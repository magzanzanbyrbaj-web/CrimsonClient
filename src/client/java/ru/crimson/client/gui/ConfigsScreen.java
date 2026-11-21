
package ru.crimson.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.ConfigManager;

import java.util.List;

public class ConfigsScreen extends Screen {

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

    @Override
    protected void init() {
        panelW = Math.min(420, width - 30);
        panelH = Math.min(340, height - 30);

        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        nameBox = new EditBox(
                font,
                panelX + 18,
                panelY + 55,
                panelW - 36,
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
        g.drawString(font, label, x + 9, y + 8, WHITE, false);
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

        g.drawString(
                font,
                "CRIMSON CONFIGS",
                panelX + 18,
                panelY + 19,
                RED,
                false
        );

        g.drawString(
                font,
                "Создание и управление конфигурациями",
                panelX + 18,
                panelY + 35,
                MUTED,
                false
        );

        int listY = panelY + 90;
        int listBottom = panelY + panelH - 100;

        if (configs.isEmpty()) {
            g.drawString(
                    font,
                    "Нет сохранённых конфигов",
                    panelX + 18,
                    listY + 5,
                    MUTED,
                    false
            );
        }

        for (int i = 0; i < configs.size(); i++) {
            int y = listY + i * 23;

            if (y + 20 > listBottom) {
                break;
            }

            boolean active = selected == i;

            g.fill(
                    panelX + 15,
                    y,
                    panelX + panelW - 15,
                    y + 20,
                    active ? 0xFF352027 : 0xFF191A1F
            );

            g.drawString(
                    font,
                    configs.get(i),
                    panelX + 24,
                    y + 6,
                    active ? RED : WHITE,
                    false
            );
        }

        int buttonY = panelY + panelH - 65;
        int buttonW = (panelW - 48) / 3;

        drawButton(
                g, "Сохранить",
                panelX + 15, buttonY,
                buttonW, 25, 0xFF252027
        );

        drawButton(
                g, "Загрузить",
                panelX + 24 + buttonW, buttonY,
                buttonW, 25, 0xFF252027
        );

        drawButton(
                g, "Удалить",
                panelX + 33 + buttonW * 2, buttonY,
                buttonW, 25, 0xFF252027
        );

        if (!status.isEmpty()) {
            g.drawString(
                    font,
                    status,
                    panelX + 18,
                    panelY + panelH - 31,
                    RED,
                    false
            );
        }

        g.drawString(
                font,
                "ESC — назад",
                panelX + 18,
                panelY + panelH - 17,
                MUTED,
                false
        );

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

        int listY = panelY + 90;
        int listBottom = panelY + panelH - 100;

        for (int i = 0; i < configs.size(); i++) {
            int y = listY + i * 23;

            if (y + 20 > listBottom) {
                break;
            }

            if (inside(
                    mx, my,
                    panelX + 15, y,
                    panelW - 30, 20
            )) {
                selected = i;
                status = "";
                return true;
            }
        }

        int buttonY = panelY + panelH - 65;
        int buttonW = (panelW - 48) / 3;

        if (my >= buttonY && my <= buttonY + 25) {

            if (inside(
                    mx, my,
                    panelX + 15, buttonY,
                    buttonW, 25
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
                    panelX + 24 + buttonW,
                    buttonY,
                    buttonW, 25
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
                    panelX + 33 + buttonW * 2,
                    buttonY,
                    buttonW, 25
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
