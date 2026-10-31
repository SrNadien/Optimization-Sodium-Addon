package nadiendev.osa.client;

import nadiendev.osa.OptimizerSodiumAddon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class JoinTimer {
    private static long connectStart = -1;
    private static long phaseStart = -1;
    private static long recipesStart = -1;
    private static long tagsStart = -1;
    private static boolean waitingForWorld = false;
    private static final List<String> lines = new ArrayList<>();

    private JoinTimer() {}

    private static long now() {
        return System.nanoTime();
    }

    private static String secs(long nanos) {
        return String.format("%.1f s", nanos / 1_000_000_000.0);
    }

    private static void mark(String label, long nanos) {
        lines.add(label + ": " + secs(nanos));
    }

    public static void onConnectScreen() {
        if (connectStart < 0) {
            connectStart = now();
            phaseStart = connectStart;
            lines.clear();
        }
    }

    public static void onLoggingIn() {
        long t = now();
        if (connectStart < 0) {
            connectStart = t;
        } else {
            mark("Conexion + login + sincronizar registros", t - phaseStart);
        }
        phaseStart = t;
        waitingForWorld = true;
    }

    public static void recipesBegin() {
        recipesStart = now();
    }

    public static void recipesEnd() {
        if (recipesStart > 0 && connectStart > 0) {
            mark("Procesando recetas (incluye los mods que reaccionan, como JEI)", now() - recipesStart);
        }
    }

    public static void tagsBegin() {
        tagsStart = now();
    }

    public static void tagsEnd() {
        if (tagsStart > 0 && connectStart > 0) {
            mark("Procesando tags", now() - tagsStart);
        }
    }

    public static void tick(Minecraft mc) {
        if (!waitingForWorld) return;
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        long t = now();
        mark("Desde login hasta poder jugar (chunks, quests, resto)", t - phaseStart);
        long total = t - connectStart;
        waitingForWorld = false;

        OptimizerSodiumAddon.LOG.info("[OSA] ===== Tiempo de entrada al servidor: {} =====", secs(total));
        for (String l : lines) OptimizerSodiumAddon.LOG.info("[OSA]   {}", l);

        mc.player.sendSystemMessage(Component.literal("[OSA] Entraste en " + secs(total) + " (detalle en logs/latest.log)")
                .withStyle(ChatFormatting.AQUA));
        for (String l : lines) {
            mc.player.sendSystemMessage(Component.literal("  " + l).withStyle(ChatFormatting.GRAY));
        }

        connectStart = -1;
        recipesStart = -1;
        tagsStart = -1;
        lines.clear();
    }

    public static void onLogout() {
        connectStart = -1;
        recipesStart = -1;
        tagsStart = -1;
        waitingForWorld = false;
        lines.clear();
    }
}
