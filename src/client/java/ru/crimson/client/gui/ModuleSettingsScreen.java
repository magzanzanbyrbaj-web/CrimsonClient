
package ru.crimson.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ModuleSettingsScreen extends Screen {
    private final Screen parent;
    private final String module;
    private final CrimsonConfig cfg;
    private final List<Setting> settings = new ArrayList<>();

    private int panelX, panelY, panelW, panelH;
    private boolean waitingForBind = false;

    private static final int RED = 0xFFFF465F;
    private static final int WHITE = 0xFFF2F2F5;
    private static final int MUTED = 0xFF999BA8;

    public ModuleSettingsScreen(Screen parent, String module) {
        super(Component.literal("Crimson Module Settings"));
        this.parent = parent;
        this.module = module;
        this.cfg = CrimsonClientClient.config;
        buildSettings();
    }

    private void toggle(String name, Supplier<Boolean> getter, Runnable action) {
        settings.add(new Setting(
                name,
                () -> getter.get() ? "ВКЛ" : "ВЫКЛ",
                action
        ));
    }

    private void cycle(String name, Supplier<String> value, Runnable action) {
        settings.add(new Setting(name, value, action));
    }

    private String bindText() {
        int key = cfg.getBind(module);

        if (waitingForBind) {
            return "НАЖМИ КЛАВИШУ...";
        }

        return key < 0 ? "НЕТ" : keyName(key);
    }

    private void buildSettings() {
        settings.clear();

        settings.add(new Setting(
                "Бинд функции",
                this::bindText,
                () -> waitingForBind = true
        ));

        switch (module) {
            case "KillAura" -> {
                cycle("Дальность", () -> String.format("%.1f", cfg.kaRange),
                        () -> cfg.kaRange = next(cfg.kaRange, 2.0, 6.0, 0.2));
                cycle("CPS", () -> String.valueOf(cfg.kaCps),
                        () -> cfg.kaCps = next(cfg.kaCps, 1, 20, 1));
                cycle("FOV", () -> String.valueOf((int) cfg.kaFov),
                        () -> cfg.kaFov = next(cfg.kaFov, 30, 180, 15));
                cycle("Плавность", () -> String.format("%.2f", cfg.kaSmoothness),
                        () -> cfg.kaSmoothness = next(cfg.kaSmoothness, 0.05f, 1.0f, 0.05f));
                cycle("Приоритет", () -> String.valueOf(cfg.kaPriority),
                        () -> cfg.kaPriority = (cfg.kaPriority + 1) % 4);
                toggle("Игроки", () -> cfg.kaPlayers,
                        () -> cfg.kaPlayers = !cfg.kaPlayers);
                toggle("Мобы", () -> cfg.kaMobs,
                        () -> cfg.kaMobs = !cfg.kaMobs);
                toggle("Только критические удары", () -> cfg.kaOnlyCriticals,
                        () -> cfg.kaOnlyCriticals = !cfg.kaOnlyCriticals);
                toggle("Сквозь стены", () -> cfg.kaThroughWalls,
                        () -> cfg.kaThroughWalls = !cfg.kaThroughWalls);
            }

            case "AimAssist" -> {
                cycle("Дальность", () -> String.format("%.1f", cfg.aaRange),
                        () -> cfg.aaRange = next(cfg.aaRange, 1.0, 6.0, 0.2));
                cycle("Скорость", () -> String.format("%.2f", cfg.aaSpeed),
                        () -> cfg.aaSpeed = next(cfg.aaSpeed, 0.05f, 1.0f, 0.05f));
                cycle("FOV", () -> String.valueOf((int) cfg.aaFov),
                        () -> cfg.aaFov = next(cfg.aaFov, 20, 180, 10));
                toggle("Игроки", () -> cfg.aaPlayers,
                        () -> cfg.aaPlayers = !cfg.aaPlayers);
                toggle("Мобы", () -> cfg.aaMobs,
                        () -> cfg.aaMobs = !cfg.aaMobs);
                toggle("Сквозь стены", () -> cfg.aaThroughWalls,
                        () -> cfg.aaThroughWalls = !cfg.aaThroughWalls);
            }

            case "TriggerBot" -> {
                cycle("Дальность", () -> String.format("%.1f", cfg.tbRange),
                        () -> cfg.tbRange = next(cfg.tbRange, 1.0, 6.0, 0.2));
                cycle("CPS", () -> String.valueOf(cfg.tbCps),
                        () -> cfg.tbCps = next(cfg.tbCps, 1, 20, 1));
                toggle("Игроки", () -> cfg.tbPlayers,
                        () -> cfg.tbPlayers = !cfg.tbPlayers);
                toggle("Мобы", () -> cfg.tbMobs,
                        () -> cfg.tbMobs = !cfg.tbMobs);
                toggle("Только критические удары", () -> cfg.tbOnlyCriticals,
                        () -> cfg.tbOnlyCriticals = !cfg.tbOnlyCriticals);
            }

            case "ESP" -> {
                toggle("Игроки", () -> cfg.espPlayers,
                        () -> cfg.espPlayers = !cfg.espPlayers);
                toggle("Мобы", () -> cfg.espMobs,
                        () -> cfg.espMobs = !cfg.espMobs);
                toggle("Сквозь стены", () -> cfg.espThroughWalls,
                        () -> cfg.espThroughWalls = !cfg.espThroughWalls);
            }

            case "ChestESP" -> {
                toggle("Обычные сундуки", () -> cfg.chestEspNormal,
                        () -> cfg.chestEspNormal = !cfg.chestEspNormal);
                toggle("Ловушки", () -> cfg.chestEspTrapped,
                        () -> cfg.chestEspTrapped = !cfg.chestEspTrapped);
                toggle("Эндер-сундуки", () -> cfg.chestEspEnder,
                        () -> cfg.chestEspEnder = !cfg.chestEspEnder);
                toggle("Бочки", () -> cfg.chestEspBarrel,
                        () -> cfg.chestEspBarrel = !cfg.chestEspBarrel);
                toggle("Шалкеры", () -> cfg.chestEspShulker,
                        () -> cfg.chestEspShulker = !cfg.chestEspShulker);
                toggle("Заливка", () -> cfg.chestEspFilled,
                        () -> cfg.chestEspFilled = !cfg.chestEspFilled);
                cycle("Толщина линий",
                        () -> String.format("%.1f", cfg.chestEspLineWidth),
                        () -> cfg.chestEspLineWidth =
                                next(cfg.chestEspLineWidth, 0.5f, 4.0f, 0.5f));
            }

            case "TargetESP" -> {
                cycle("Масштаб", () -> String.format("%.1f", cfg.targetEspScale),
                        () -> cfg.targetEspScale =
                                next(cfg.targetEspScale, 0.5f, 2.5f, 0.1f));
                toggle("Кольца", () -> cfg.targetEspRing,
                        () -> cfg.targetEspRing = !cfg.targetEspRing);
                cycle("Цвет", () -> colorName(cfg.targetEspColor),
                        () -> cfg.targetEspColor = nextColor(cfg.targetEspColor));
            }

            case "HitSound" -> {
                cycle("Звук", () -> soundName(cfg.hitSoundType),
                        () -> cfg.hitSoundType = (cfg.hitSoundType + 1) % 7);
                cycle("Громкость",
                        () -> Math.round(cfg.hitSoundVolume * 100) + "%",
                        () -> cfg.hitSoundVolume =
                                next(cfg.hitSoundVolume, 0.0f, 1.0f, 0.1f));
            }

            case "NoRender" -> {
                toggle("Огонь", () -> cfg.hideFire,
                        () -> cfg.hideFire = !cfg.hideFire);
                toggle("Тряска камеры", () -> cfg.hideHurtCamera,
                        () -> cfg.hideHurtCamera = !cfg.hideHurtCamera);
                toggle("Портал", () -> cfg.hidePortal,
                        () -> cfg.hidePortal = !cfg.hidePortal);
                toggle("Тыква", () -> cfg.hidePumpkin,
                        () -> cfg.hidePumpkin = !cfg.hidePumpkin);
                toggle("Виньетка", () -> cfg.hideVignette,
                        () -> cfg.hideVignette = !cfg.hideVignette);
                toggle("Тошнота", () -> cfg.hideNausea,
                        () -> cfg.hideNausea = !cfg.hideNausea);
                toggle("Погода", () -> cfg.hideWeather,
                        () -> cfg.hideWeather = !cfg.hideWeather);
                toggle("Частицы", () -> cfg.hideParticles,
                        () -> cfg.hideParticles = !cfg.hideParticles);
            }

            case "SmoothSword" -> {
                cycle("Стиль анимации", () -> String.valueOf(cfg.swingStyle + 1),
                        () -> cfg.swingStyle = (cfg.swingStyle + 1) % 4);
                cycle("Скорость", () -> String.format("%.1f", cfg.swingSpeed),
                        () -> cfg.swingSpeed = next(cfg.swingSpeed, 0.5f, 3.0f, 0.1f));
                cycle("Размах", () -> String.format("%.1f", cfg.swingScale),
                        () -> cfg.swingScale = next(cfg.swingScale, 0.5f, 2.0f, 0.1f));
                toggle("Только меч", () -> cfg.swingOnlySword,
                        () -> cfg.swingOnlySword = !cfg.swingOnlySword);
            }

            default -> settings.add(new Setting(
                    "Для этой функции настроек пока нет",
                    () -> "—",
                    () -> {}
            ));
        }
    }

    @Override
    protected void init() {
        panelW = Math.min(390, width - 24);
        panelH = Math.min(430, height - 24);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xAA05060A);
        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF171820);
        g.fill(panelX, panelY, panelX + panelW, panelY + 2, RED);

        g.drawString(font, "НАСТРОЙКИ ФУНКЦИИ",
                panelX + 16, panelY + 14, RED, false);
        g.drawString(font, module,
                panelX + 16, panelY + 32, WHITE, false);

        g.drawString(font,
                waitingForBind
                        ? "Нажми клавишу для назначения бинда"
                        : "Нажимай на параметр, чтобы изменить значение",
                panelX + 16, panelY + 49, MUTED, false);

        int rowY = panelY + 72;
        int rowH = 29;
        int maxRows = Math.max(0, (panelH - 105) / rowH);

        for (int i = 0; i < Math.min(settings.size(), maxRows); i++) {
            Setting s = settings.get(i);
            int y = rowY + i * rowH;

            boolean hover = mouseX >= panelX + 12
                    && mouseX <= panelX + panelW - 12
                    && mouseY >= y
                    && mouseY <= y + 25;

            g.fill(panelX + 12, y, panelX + panelW - 12, y + 25,
                    hover ? 0xFF30323D : 0xFF22242D);

            g.drawString(font, s.name,
                    panelX + 20, y + 8, WHITE, false);

            String value = s.value.get();
            int valueX = panelX + panelW - 22 - font.width(value);

            g.drawString(font, value, valueX, y + 8,
                    i == 0 && waitingForBind ? WHITE : RED, false);
        }

        g.drawString(font, "ESC — назад",
                panelX + 16, panelY + panelH - 18, MUTED, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int mx = (int) event.x();
            int my = (int) event.y();

            int rowY = panelY + 72;
            int rowH = 29;
            int maxRows = Math.max(0, (panelH - 105) / rowH);

            for (int i = 0; i < Math.min(settings.size(), maxRows); i++) {
                int y = rowY + i * rowH;

                if (mx >= panelX + 12
                        && mx <= panelX + panelW - 12
                        && my >= y
                        && my <= y + 25) {

                    settings.get(i).action.run();

                    if (!waitingForBind) {
                        cfg.save();
                    }

                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (waitingForBind) {
            int key = event.key();

            if (key == GLFW.GLFW_KEY_ESCAPE) {
                cfg.setBind(module, -1);
            } else if (key != GLFW.GLFW_KEY_UNKNOWN) {
                cfg.setBind(module, key);
            }

            waitingForBind = false;
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        cfg.save();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static double next(double v, double min, double max, double step) {
        double n = v + step;
        return n > max + 0.0001
                ? min
                : Math.round(n * 100.0) / 100.0;
    }

    private static float next(float v, float min, float max, float step) {
        float n = v + step;
        return n > max + 0.0001f
                ? min
                : Math.round(n * 100.0f) / 100.0f;
    }

    private static int next(int v, int min, int max, int step) {
        return v + step > max ? min : v + step;
    }

    private static String soundName(int id) {
        return switch (Math.floorMod(id, 7)) {
            case 1 -> "Critical";
            case 2 -> "Strong";
            case 3 -> "Knockback";
            case 4 -> "Arrow";
            case 5 -> "Experience";
            case 6 -> "Pling";
            default -> "Weak";
        };
    }

    private static String colorName(int color) {
        return switch (color) {
            case 0xFFFF465F -> "Красный";
            case 0xFF55CCFF -> "Голубой";
            case 0xFF55FF88 -> "Зелёный";
            case 0xFFFFFF55 -> "Жёлтый";
            default -> "Белый";
        };
    }

    private static int nextColor(int color) {
        if (color == -1) return 0xFFFF465F;
        if (color == 0xFFFF465F) return 0xFF55CCFF;
        if (color == 0xFF55CCFF) return 0xFF55FF88;
        if (color == 0xFF55FF88) return 0xFFFFFF55;
        return -1;
    }

    private static String keyName(int key) {
        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            return String.valueOf((char) ('A' + key - GLFW.GLFW_KEY_A));
        }

        if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
            return String.valueOf((char) ('0' + key - GLFW.GLFW_KEY_0));
        }

        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F12) {
            return "F" + (key - GLFW.GLFW_KEY_F1 + 1);
        }

        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_INSERT -> "INSERT";
            case GLFW.GLFW_KEY_DELETE -> "DELETE";
            default -> "KEY_" + key;
        };
    }

    private record Setting(
            String name,
            Supplier<String> value,
            Runnable action
    ) {}
}
