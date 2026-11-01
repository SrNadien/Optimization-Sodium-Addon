package nadiendev.osa.loader;

import nadiendev.osa.client.BetterGrassModels;
import net.fabricmc.api.ClientModInitializer;

public final class OsaClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientHooks.register();
        BetterGrassModels.register();
    }
}
