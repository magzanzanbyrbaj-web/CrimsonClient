package ru.crimson.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CrimsonMenuScreen extends Screen {

	private final long openTime;
	private float animProgress = 0f;
	private final List<GlassButton> buttons = new ArrayList<>();
	private final CrimsonConfig config;
	private int currentTab = 0; // 0=HUD, 1=Visuals, 2=Utilities

	private static final String[] TABS = {"HUD", "Visuals", "Utilities"};

	// Red glass palette
	private static final int BG_DARK = 0xCC0A0505;
	private static final int PANEL_GLASS = 0xAA3A0A0A;
	private static final int PANEL_BORDER = 0xFFAA2222;
	private static final int ACCENT = 0xFFFF3333;
	private static final int TEXT_MAIN = 0xFFFFEEEE;
	private static final int TEXT_DIM = 0xFFCCAAAA;
	private static final int TOGGLE_ON = 0xFF44FF66;
	private static final int TOGGLE_OFF = 0xFFAA3333;

	public CrimsonMenuScreen() {
		super(Component.literal("Crimson Client"));
		this.openTime = System.currentTimeMillis();
		this.config = CrimsonClientClient.config;
		playOpenSound();
	}

	private void playOpenSound() {
		Minecraft.getInstance().getSoundManager().play(
				SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.2f, 0.7f)
		);
	}

	private void playClickSound() {
		Minecraft.getInstance().getSoundManager().play(
				SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f)
		);
	}

	@Override
	protected void init() {
		buttons.clear();
		int centerX = this.width / 2;
		int panelW = 220;
		int startY = this.height / 2 - 110;
		int btnW = 200;
		int btnH = 22;
		int gap = 4;
		int left = centerX - panelW / 2 + 10;

		// Tab buttons at top
		int tabW = 70;
		for (int i = 0; i < TABS.length; i++) {
			final int tab = i;
			buttons.add(new GlassButton(centerX - (TABS.length * tabW) / 2 + i * tabW, startY - 30, tabW - 4, 20,
					TABS[i], currentTab == i, () -> {
				currentTab = tab;
				playClickSound();
				this.init();
			}, true));
		}

		List<ToggleEntry> entries = new ArrayList<>();
		if (currentTab == 0) { // HUD
			entries.add(new ToggleEntry("Watermark (Name+FPS+Ping)", () -> config.watermark, v -> config.watermark = v));
			entries.add(new ToggleEntry("Target HUD", () -> config.targetHud, v -> config.targetHud = v));
			entries.add(new ToggleEntry("Armor HUD", () -> config.armorHud, v -> config.armorHud = v));
			entries.add(new ToggleEntry("Totem Counter", () -> config.totemCounter, v -> config.totemCounter = v));
			entries.add(new ToggleEntry("Keystrokes", () -> config.keystrokes, v -> config.keystrokes = v));
			entries.add(new ToggleEntry("Module List", () -> config.moduleList, v -> config.moduleList = v));
			entries.add(new ToggleEntry("Inventory HUD", () -> config.inventoryHud, v -> config.inventoryHud = v));
			entries.add(new ToggleEntry("Potion HUD", () -> config.potionHud, v -> config.potionHud = v));
			entries.add(new ToggleEntry("Durability HUD", () -> config.durabilityHud, v -> config.durabilityHud = v));
		} else if (currentTab == 1) { // Visuals
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
		} else { // Utilities
			entries.add(new ToggleEntry("Fullbright", () -> config.fullbright, v -> config.fullbright = v));
			entries.add(new ToggleEntry("Reduce Particles", () -> config.reduceParticles, v -> config.reduceParticles = v));
			entries.add(new ToggleEntry("Optimization Mode", () -> config.optimized, v -> config.optimized = v));
			entries.add(new ToggleEntry("Menu Animation", () -> config.glassAnimation, v -> config.glassAnimation = v));
		}

		int y = startY + 5;
		for (ToggleEntry e : entries) {
			buttons.add(new GlassButton(left, y, btnW, btnH, e.name, e.getter.getAsBoolean(), () -> {
				e.setter.accept(!e.getter.getAsBoolean());
				config.save();
				playClickSound();
			}));
			y += btnH + gap;
		}

		// Close
		buttons.add(new GlassButton(centerX - 50, y + 10, 100, 24, "Close", false, () -> {
			playClickSound();
			this.onClose();
		}, true));
	}

	private static class ToggleEntry {
		String name;
		BooleanSupplier getter;
		Consumer<Boolean> setter;
		ToggleEntry(String name, BooleanSupplier g, Consumer<Boolean> s) {
			this.name = name;
			this.getter = g;
			this.setter = s;
		}
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		// Dark overlay
		g.fill(0, 0, this.width, this.height, 0x99000000);

		float t = (System.currentTimeMillis() - openTime) / 300f;
		animProgress = config.glassAnimation ? Mth.clamp(t, 0f, 1f) : 1f;
		float alpha = easeOutCubic(animProgress);

		int centerX = this.width / 2;
		int panelW = 240;
		int panelH = 280;
		int panelX = centerX - panelW / 2;
		int panelY = this.height / 2 - panelH / 2 - 10;

		// Panel background
		g.fill(panelX, panelY, panelX + panelW, panelY + panelH, withAlpha(PANEL_GLASS, alpha));
		// Border
		g.fill(panelX, panelY, panelX + panelW, panelY + 2, withAlpha(ACCENT, alpha));
		g.fill(panelX, panelY + panelH - 2, panelX + panelW, panelY + panelH, withAlpha(ACCENT, alpha));
		g.fill(panelX, panelY, panelX + 2, panelY + panelH, withAlpha(ACCENT, alpha));
		g.fill(panelX + panelW - 2, panelY, panelX + panelW, panelY + panelH, withAlpha(ACCENT, alpha));

		// Title
		String title = "Crimson Client";
		int tw = this.font.width(title);
		g.drawString(this.font, title, centerX - tw / 2, panelY + 8, withAlpha(TEXT_MAIN, alpha), true);
		g.drawString(this.font, "Right Shift to open", centerX - this.font.width("Right Shift to open") / 2, panelY + 20, withAlpha(TEXT_DIM, alpha), false);

		// Render buttons
		for (GlassButton btn : buttons) {
			btn.render(g, mouseX, mouseY, alpha, this.font);
		}

		super.render(g, mouseX, mouseY, partialTick);
	}

	private float easeOutCubic(float t) {
		return 1f - (float) Math.pow(1 - t, 3);
	}

	private int withAlpha(int color, float alphaMul) {
		int a = (color >> 24) & 0xFF;
		if (a == 0) a = 0xFF;
		a = (int) (a * alphaMul);
		return (a << 24) | (color & 0x00FFFFFF);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			for (GlassButton btn : buttons) {
				if (btn.isHovered((int) mouseX, (int) mouseY)) {
					btn.onClick.run();
					this.init();
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
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

	// Simple glass-style button
	private static class GlassButton {
		int x, y, w, h;
		String label;
		boolean toggled;
		boolean isClose;
		Runnable onClick;

		GlassButton(int x, int y, int w, int h, String label, boolean toggled, Runnable onClick) {
			this(x, y, w, h, label, toggled, onClick, false);
		}

		GlassButton(int x, int y, int w, int h, String label, boolean toggled, Runnable onClick, boolean isClose) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.label = label;
			this.toggled = toggled;
			this.onClick = onClick;
			this.isClose = isClose;
		}

		boolean isHovered(int mx, int my) {
			return mx >= x && mx <= x + w && my >= y && my <= y + h;
		}

		void render(GuiGraphics g, int mx, int my, float alpha, net.minecraft.client.gui.Font font) {
			boolean hover = isHovered(mx, my);
			int bg = hover ? 0xCC5A1515 : 0xAA2A0A0A;
			int borderCol = hover ? ACCENT : PANEL_BORDER;

			g.fill(x, y, x + w, y + h, withAlpha(bg, alpha));
			g.fill(x, y, x + w, y + 1, withAlpha(borderCol, alpha));
			g.fill(x, y + h - 1, x + w, y + h, withAlpha(borderCol, alpha));
			g.fill(x, y, x + 1, y + h, withAlpha(borderCol, alpha));
			g.fill(x + w - 1, y, x + w, y + h, withAlpha(borderCol, alpha));

			int textColor = isClose ? TEXT_DIM : TEXT_MAIN;
			int tw = font.width(label);
			g.drawString(font, label, x + (w - tw) / 2, y + (h - 8) / 2, withAlpha(textColor, alpha), false);

			if (!isClose) {
				String state = toggled ? "ON" : "OFF";
				int stateCol = toggled ? TOGGLE_ON : TOGGLE_OFF;
				g.drawString(font, state, x + w - font.width(state) - 6, y + (h - 8) / 2, withAlpha(stateCol, alpha), false);
			}
		}

		private int withAlpha(int color, float alphaMul) {
			int a = (color >> 24) & 0xFF;
			if (a == 0) a = 0xFF;
			a = (int) (a * alphaMul);
			return (a << 24) | (color & 0x00FFFFFF);
		}
	}
                                  }
