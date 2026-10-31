package nadiendev.osa;

import com.mojang.logging.LogUtils;
import nadiendev.osa.config.OsaConfig;
import nadiendev.osa.loader.Platform;
import nadiendev.osa.loader.ServerHooks;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public final class OptimizerSodiumAddon implements ModInitializer {
    public static final String MODID = "optimizersodiumaddon";
    public static final Logger LOG = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        OsaConfig.load();
        ServerHooks.register();
        LOG.info("Optimizer Sodium Addon cargado (Sodium presente: {})",
                Platform.isModLoaded("sodium"));
    }
}
