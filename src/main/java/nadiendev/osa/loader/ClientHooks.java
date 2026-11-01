package nadiendev.osa.loader;

import nadiendev.osa.client.AutoTune;
import nadiendev.osa.client.BetterGrassModels;
import nadiendev.osa.client.CullCommands;
import nadiendev.osa.client.DynamicLights;
import nadiendev.osa.client.EntityCulling;
import nadiendev.osa.client.GpuLeakCleaner;
import nadiendev.osa.client.JoinTimer;
import nadiendev.osa.client.ParticleLimits;
import nadiendev.osa.client.ReloadCommands;
import nadiendev.osa.client.ReloadTimer;
import nadiendev.osa.client.RenderLimits;
import nadiendev.osa.client.StartupTimer;
import nadiendev.osa.client.TintCoalescer;
import nadiendev.osa.client.TuneCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;

public final class ClientHooks {
    private ClientHooks() {}

    public static void register() {
        ModelLoadingPlugin.register(new BetterGrassModels());

        ClientTickEvents.START_CLIENT_TICK.register(client -> ParticleLimits.resetTick());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            RenderLimits.tick(client);
            GpuLeakCleaner.tick();
            JoinTimer.tick(client);
            AutoTune.tick(client);
            DynamicLights.tick(client);
            TintCoalescer.tick(client);
        });

        ScreenEvents.BEFORE_INIT.register((client, screen, width, height) -> {
            if (screen instanceof ConnectScreen) JoinTimer.onConnectScreen();
        });
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            ScreenEvents.afterRender(screen).register((current, graphics, mouseX, mouseY, tickDelta) -> {
                ReloadTimer.drawDone(graphics);
                StartupTimer.draw(current, graphics);
            });
        });
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            if (Minecraft.getInstance().screen == null) ReloadTimer.drawDone(graphics);
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            ReloadCommands.register(dispatcher);
            TuneCommands.register(dispatcher);
            CullCommands.register(dispatcher);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> JoinTimer.onLoggingIn());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            RenderLimits.clear();
            DynamicLights.clear();
            TintCoalescer.clear();
            EntityCulling.clear();
            JoinTimer.onLogout();
        });
    }
}
