package nadiendev.osa.loader;

import nadiendev.osa.client.GpuLeakCleaner;
import nadiendev.osa.client.JoinTimer;
import nadiendev.osa.client.ParticleLimits;
import nadiendev.osa.client.RenderLimits;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.ConnectScreen;

public final class ClientHooks {
    private ClientHooks() {}

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> ParticleLimits.resetTick());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            RenderLimits.tick(client);
            GpuLeakCleaner.tick();
            JoinTimer.tick(client);
        });

        ScreenEvents.BEFORE_INIT.register((client, screen, width, height) -> {
            if (screen instanceof ConnectScreen) JoinTimer.onConnectScreen();
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> JoinTimer.onLoggingIn());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            RenderLimits.clear();
            JoinTimer.onLogout();
        });
    }
}
