package nadiendev.osa;

import com.mojang.logging.LogUtils;
import nadiendev.osa.client.ClientSetup;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(OptimizerSodiumAddon.MODID)
public final class OptimizerSodiumAddon {
    public static final String MODID = "optimizersodiumaddon";
    public static final Logger LOG = LogUtils.getLogger();

    public OptimizerSodiumAddon(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, OsaConfig.SERVER_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, OsaConfig.CLIENT_SPEC);
        container.registerConfig(ModConfig.Type.COMMON, OsaConfig.COMMON_SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT) ClientSetup.register(container);

        modBus.addListener(ModConfigEvent.Loading.class, e -> OsaConfig.invalidateCaches());
        modBus.addListener(ModConfigEvent.Reloading.class, e -> OsaConfig.invalidateCaches());

        LOG.info("Optimizer Sodium Addon cargado (Sodium presente: {}, Compact Machines presente: {})",
                ModList.get().isLoaded("sodium"), ModList.get().isLoaded("compactmachines"));
    }
}
