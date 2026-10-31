package nadiendev.osa.loader;

import net.fabricmc.api.ClientModInitializer;

public final class OsaClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientHooks.register();
    }
}
