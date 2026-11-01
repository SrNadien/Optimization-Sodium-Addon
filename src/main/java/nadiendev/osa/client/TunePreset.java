package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import net.minecraft.client.CloudStatus;
import net.minecraft.server.level.ParticleStatus;

public record TunePreset(
        int renderDistance, int simulationDistance, boolean cutoutLeaves, int weatherRadius, boolean vignette,
        boolean ambientOcclusion, int biomeBlend, boolean vsync, double entityDistance, boolean entityShadows,
        int mipmaps, ParticleStatus particles, CloudStatus clouds, int maxFps, int menuBlur,
        int armorMax, int armorDistance, int itemMax, int itemDistance, int xpMax, int xpDistance,
        int particleTotal, int particlePerTick, int particleDistance,
        boolean fastMath, SodiumValues sodium) {

    public record SodiumValues(boolean stagingBuffers, int renderAhead, boolean animateVisibleOnly,
                               boolean entityCulling, boolean fogOcclusion, boolean blockFaceCulling,
                               boolean noErrorContext) {
        static SodiumValues minimal() {
            return new SodiumValues(false, 0, false, false, false, false, false);
        }

        static SodiumValues standard() {
            return new SodiumValues(true, 3, true, true, true, true, true);
        }
    }

    public static TunePreset of(TuneProfile tier, HardwareProfile hardware) {
        OsaConfig.Client.AutoTune config = OsaConfig.CLIENT.autoTune;
        int refresh = Math.min(260, Math.max(10, (hardware.refreshRate() + 5) / 10 * 10));
        return switch (tier) {
            case LOW -> new TunePreset(config.renderDistanceLow, 5, false, 3, false, false, 0, false, 0.5, false, 0,
                    ParticleStatus.MINIMAL, CloudStatus.OFF, 260, 2,
                    12, 16, 48, 20, 24, 16, 1200, 120, 24,
                    true, SodiumValues.minimal());
            case MEDIUM -> new TunePreset(config.renderDistanceMedium, 8, true, 6, true, true, 3, true, 1.0, true, 2,
                    ParticleStatus.DECREASED, CloudStatus.FAST, refresh, 5,
                    32, 24, 150, 32, 60, 24, 3000, 400, 48,
                    false, SodiumValues.standard());
            default -> new TunePreset(config.renderDistanceHigh, 12, true, 10, true, true, 5, true, 1.0, true, 4,
                    ParticleStatus.ALL, CloudStatus.FANCY, 260, 5,
                    64, 32, 300, 48, 120, 32, 6000, 800, 64,
                    false, SodiumValues.standard());
        };
    }
}
