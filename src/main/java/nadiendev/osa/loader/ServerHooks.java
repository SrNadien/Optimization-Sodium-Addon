package nadiendev.osa.loader;

import nadiendev.osa.command.OsaCommands;
import nadiendev.osa.server.AsyncSaver;
import nadiendev.osa.server.LagAlerts;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class ServerHooks {
    private ServerHooks() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> OsaCommands.register(dispatcher));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> LagAlerts.reset());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> AsyncSaver.flush());
        ServerTickEvents.END_SERVER_TICK.register(LagAlerts::tick);
    }
}
