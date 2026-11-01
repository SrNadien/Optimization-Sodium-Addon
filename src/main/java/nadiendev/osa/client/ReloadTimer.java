package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

public final class ReloadTimer {
    private static final long SHOW_MILLIS = 8000L;
    private static final int MARGIN = 4;
    private static final int PADDING = 3;

    private static volatile boolean visible;
    private static volatile boolean running;
    private static volatile boolean completed;
    private static volatile boolean chat;
    private static volatile long startNanos;
    private static volatile long lastNanos;
    private static volatile long initialNanos;
    private static volatile long hideAtMillis;

    private ReloadTimer() {}

    static void begin(int id, long start) {
        visible = id > 1;
        running = true;
        startNanos = start;
    }

    static void end(long total) {
        if (!visible) {
            initialNanos = total;
            StartupTimer.markReady();
        }
        lastNanos = total;
        completed = true;
        running = false;
        hideAtMillis = Util.getMillis() + SHOW_MILLIS;
        if (chat && visible) announce();
    }

    public static long initial() {
        return initialNanos;
    }

    public static boolean chatEnabled() {
        return chat;
    }

    public static void setChat(boolean enabled) {
        chat = enabled;
    }

    public static boolean hasLast() {
        return completed;
    }

    public static String last() {
        return ReloadProfiler.fmt(lastNanos);
    }

    private static void announce() {
        Minecraft minecraft = Minecraft.getInstance();
        Component message = Component.literal("[OSA] Recarga de recursos: " + last());
        minecraft.execute(() -> {
            if (minecraft.level != null) minecraft.gui.getChat().addMessage(message);
        });
    }

    public static void drawLive(GuiGraphics graphics) {
        if (!visible || !running || !OsaConfig.reloadCounterEnabled()) return;
        draw(graphics, Component.translatable("optimizersodiumaddon.counter.running", ReloadProfiler.fmt(System.nanoTime() - startNanos)));
    }

    public static void drawDone(GuiGraphics graphics) {
        if (!visible || running || !OsaConfig.reloadCounterEnabled() || Util.getMillis() >= hideAtMillis) return;
        draw(graphics, Component.translatable("optimizersodiumaddon.counter.done", ReloadProfiler.fmt(lastNanos)));
    }

    private static void draw(GuiGraphics graphics, Component text) {
        Font font = Minecraft.getInstance().font;
        int width = font.width(text);
        graphics.fill(MARGIN - PADDING, MARGIN - PADDING, MARGIN + width + PADDING, MARGIN + font.lineHeight + PADDING, 0xA0000000);
        graphics.drawString(font, text, MARGIN, MARGIN, 0xFFFFFF55, true);
    }
}
