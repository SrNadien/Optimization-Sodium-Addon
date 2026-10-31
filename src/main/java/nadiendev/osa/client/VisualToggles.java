package nadiendev.osa.client;

import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.OsaConfig;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.MobEffectFogEnvironment;
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
            case ATMOSPHERIC -> !cfg.terrainFog.get();
            case WATER -> !cfg.waterFog.get();
            case LAVA -> !cfg.lavaFog.get();
            case POWDER_SNOW -> !cfg.powderSnowFog.get();
            case NONE -> false;
        };
        if (!remove) return;

        if (event.getEnvironment() instanceof MobEffectFogEnvironment) return;

        FogData fog = event.getFogData();
        fog.environmentalStart = NO_FOG_NEAR;
        fog.environmentalEnd = NO_FOG_FAR;
        fog.renderDistanceStart = NO_FOG_NEAR;
        fog.renderDistanceEnd = NO_FOG_FAR;
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
