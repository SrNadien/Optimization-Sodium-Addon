package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public final class TintCoalescer {
    private static long ticks;
    private static long lastRun;
    private static boolean pending;
    private static boolean passing;

    private TintCoalescer() {}

    public static boolean defer() {
        if (passing || !OsaConfig.coalesceTintsEnabled()) return false;
        if (ticks - lastRun >= OsaConfig.tintIntervalTicks()) {
            lastRun = ticks;
            pending = false;
            return false;
        }
        pending = true;
        return true;
    }

    public static void tick(Minecraft minecraft) {
        ticks++;
        if (!pending) return;
        ClientLevel level = minecraft.level;
        if (level == null) {
            pending = false;
            return;
        }
        if (ticks - lastRun < OsaConfig.tintIntervalTicks()) return;
        pending = false;
        lastRun = ticks;
        passing = true;
        try {
            level.clearTintCaches();
        } finally {
            passing = false;
        }
    }

    public static void clear() {
        pending = false;
    }
}
