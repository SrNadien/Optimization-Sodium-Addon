package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import net.minecraft.client.Camera;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class VisualToggles {
    private static final float NO_FOG_NEAR = 1_000_000.0F;
    private static final float NO_FOG_FAR = 1_100_000.0F;

    private VisualToggles() {}

    public static boolean removeFog(Camera camera) {
        if (!OsaConfig.clientReady()) return false;
        OsaConfig.Client.Fog fog = OsaConfig.CLIENT.fog;
        boolean remove = switch (camera.getFluidInCamera()) {
            case NONE -> !fog.terrain;
            case WATER -> !fog.water;
            case LAVA -> !fog.lava;
            case POWDER_SNOW -> !fog.powderSnow;
        };
        if (!remove) return false;

        Entity entity = camera.getEntity();
        return !(entity instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS)));
    }

    public static float fogStart(Camera camera, float vanilla) {
        return removeFog(camera) ? NO_FOG_NEAR : vanilla;
    }

    public static float fogEnd(Camera camera, float vanilla) {
        return removeFog(camera) ? NO_FOG_FAR : vanilla;
    }

    public static boolean hideWaterOverlay() {
        return OsaConfig.clientReady() && !OsaConfig.CLIENT.overlays.water;
    }

    public static boolean hideFireOverlay() {
        return OsaConfig.clientReady() && !OsaConfig.CLIENT.overlays.fire;
    }
}
