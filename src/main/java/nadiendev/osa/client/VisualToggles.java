package nadiendev.osa.client;

import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.OsaConfig;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = OptimizerSodiumAddon.MODID, value = Dist.CLIENT)
public final class VisualToggles {
    private static final float NO_FOG_NEAR = 1_000_000.0F;
    private static final float NO_FOG_FAR = 1_100_000.0F;

    private VisualToggles() {}

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!OsaConfig.clientReady()) return;
        OsaConfig.Client cfg = OsaConfig.CLIENT;
        boolean remove = switch (event.getType()) {
            case NONE -> !cfg.terrainFog.get();
            case WATER -> !cfg.waterFog.get();
            case LAVA -> !cfg.lavaFog.get();
            case POWDER_SNOW -> !cfg.powderSnowFog.get();
        };
        if (!remove) return;

        Entity entity = event.getCamera().getEntity();
        if (entity instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS))) {
            return;
        }
        event.setNearPlaneDistance(NO_FOG_NEAR);
        event.setFarPlaneDistance(NO_FOG_FAR);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockScreenEffect(RenderBlockScreenEffectEvent event) {
        if (!OsaConfig.clientReady()) return;
        boolean hide = switch (event.getOverlayType()) {
            case WATER -> !OsaConfig.CLIENT.waterOverlay.get();
            case FIRE -> !OsaConfig.CLIENT.fireOverlay.get();
            case BLOCK -> false;
        };
        if (hide) event.setCanceled(true);
    }
}
