package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.renderer.fog.environment.MobEffectFogEnvironment;
import net.minecraft.world.level.material.FogType;

public final class VisualToggles {
    private static final float NO_FOG_NEAR = 1_000_000.0F;
    private static final float NO_FOG_FAR = 1_100_000.0F;

    private VisualToggles() {}

    public static void adjustFog(FogData fog, FogType type, FogEnvironment environment) {
        if (!OsaConfig.clientReady()) return;
        OsaConfig.Client.Fog cfg = OsaConfig.CLIENT.fog;
        boolean remove = switch (type) {
            case ATMOSPHERIC, NONE -> !cfg.terrain;
            case WATER -> !cfg.water;
            case LAVA -> !cfg.lava;
            case POWDER_SNOW -> !cfg.powderSnow;
        };
        if (!remove) return;

        if (environment instanceof MobEffectFogEnvironment) return;

        fog.environmentalStart = NO_FOG_NEAR;
        fog.environmentalEnd = NO_FOG_FAR;
        fog.renderDistanceStart = NO_FOG_NEAR;
        fog.renderDistanceEnd = NO_FOG_FAR;
    }

    public static boolean hideWaterOverlay() {
        return OsaConfig.clientReady() && !OsaConfig.CLIENT.overlays.water;
    }

    public static boolean hideFireOverlay() {
        return OsaConfig.clientReady() && !OsaConfig.CLIENT.overlays.fire;
    }
}
