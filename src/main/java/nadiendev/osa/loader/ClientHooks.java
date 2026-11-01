package nadiendev.osa.loader;

import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.client.AutoTune;
import nadiendev.osa.client.DynamicLights;
import nadiendev.osa.client.GpuLeakCleaner;
import nadiendev.osa.client.JoinTimer;
import nadiendev.osa.client.ParticleLimits;
import nadiendev.osa.client.ReloadCommands;
import nadiendev.osa.client.ReloadTimer;
import nadiendev.osa.client.RenderLimits;
import nadiendev.osa.client.TintCoalescer;
import nadiendev.osa.client.TuneCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.resources.Identifier;

public final class ClientHooks {
    private ClientHooks() {}

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> ParticleLimits.resetTick());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            RenderLimits.tick(client);
            GpuLeakCleaner.tick();
            JoinTimer.tick(client);
            AutoTune.tick(client);
            DynamicLights.tick(client);
            TintCoalescer.tick(client);
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> {
            ReloadCommands.register(dispatcher);
            TuneCommands.register(dispatcher);
        });

        ScreenEvents.BEFORE_INIT.register((client, screen, width, height) -> {
            if (screen instanceof ConnectScreen) JoinTimer.onConnectScreen();
        });
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) ->
                ScreenEvents.afterExtract(screen).register((current, graphics, mouseX, mouseY, tickDelta) ->
                        ReloadTimer.drawDone(graphics)));
        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(OptimizerSodiumAddon.MODID, "reload_timer"),
                (graphics, tracker) -> {
                    if (Minecraft.getInstance().gui.screen() == null) ReloadTimer.drawDone(graphics);
                });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> JoinTimer.onLoggingIn());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            RenderLimits.clear();
            DynamicLights.clear();
            TintCoalescer.clear();
            JoinTimer.onLogout();
        });
    }
}
