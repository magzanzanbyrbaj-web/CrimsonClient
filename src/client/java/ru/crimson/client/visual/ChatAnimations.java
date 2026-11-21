package ru.crimson.client.visual;

import net.minecraft.client.GuiMessage;
import net.minecraft.util.Mth;
import ru.crimson.client.CrimsonClientClient;
import ru.crimson.client.config.CrimsonConfig;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * ChatAnimations - плавное появление сообщений в чате.
 * Нужен миксин на ChatComponent.render / addMessage.
 */
public final class ChatAnimations {

    private static final Map<GuiMessage, Long> messageTimes = new WeakHashMap<>();
    private static final long ANIM_DURATION = 300; // мс

    private ChatAnimations() {}

    public static void onMessageAdded(GuiMessage message) {
        CrimsonConfig cfg = CrimsonClientClient.config;
        if (cfg == null || !cfg.chatAnimations) return;
        messageTimes.put(message, System.currentTimeMillis());
    }

    /**
     * Возвращает alpha и offset для сообщения.
     * @return float[2] {alpha, yOffset}
     */
    public static float[] getAnim(GuiMessage message) {
        CrimsonConfig cfg = CrimsonClientClient.config;
        if (cfg == null || !cfg.chatAnimations) return new float[]{1f, 0f};

        Long start = messageTimes.get(message);
        if (start == null) return new float[]{1f, 0f};

        long elapsed = System.currentTimeMillis() - start;
        if (elapsed >= ANIM_DURATION) {
            messageTimes.remove(message);
            return new float[]{1f, 0f};
        }

        float progress = (float) elapsed / ANIM_DURATION;
        // easeOutCubic
        float eased = 1f - (float) Math.pow(1f - progress, 3);

        float alpha = eased;
        float yOffset = (1f - eased) * 12f; // снизу вверх

        return new float[]{alpha, yOffset};
    }
}
