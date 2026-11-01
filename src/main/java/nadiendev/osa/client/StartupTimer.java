package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import nadiendev.osa.OptimizerSodiumAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.lang.management.ManagementFactory;

public final class StartupTimer {
    private static final int MARGIN = 4;
    private static final int PADDING = 3;

    private static long totalMillis = -1;

    private StartupTimer() {}

    static void markReady() {
        if (totalMillis >= 0) return;
        totalMillis = System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime();
        OptimizerSodiumAddon.LOG.info("[OSA] El juego arranco en {}", ReloadProfiler.fmt(totalMillis * 1_000_000L));
    }

    public static void draw(Screen screen, GuiGraphicsExtractor graphics) {
        if (totalMillis < 0 || !(screen instanceof TitleScreen) || !OsaConfig.startupTimerEnabled()) return;
        String total = ReloadProfiler.fmt(totalMillis * 1_000_000L);
        long resources = ReloadTimer.initial();
        Component text = resources > 0
                ? Component.translatable("optimizersodiumaddon.startup.with_resources", total, ReloadProfiler.fmt(resources))
                : Component.translatable("optimizersodiumaddon.startup.total", total);
        Font font = Minecraft.getInstance().font;
        graphics.fill(MARGIN - PADDING, MARGIN - PADDING, MARGIN + font.width(text) + PADDING, MARGIN + font.lineHeight + PADDING, 0xA0000000);
        graphics.text(font, text, MARGIN, MARGIN, 0xFFFFFF55, true);
    }
}
