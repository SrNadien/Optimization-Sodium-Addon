package nadiendev.osa.client;

import nadiendev.osa.OptimizerSodiumAddon;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = OptimizerSodiumAddon.MODID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        ParticleLimits.resetTick();
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        RenderLimits.tick(Minecraft.getInstance());
        GpuLeakCleaner.tick();
        AutoTune.tick(Minecraft.getInstance());
        DynamicLights.tick(Minecraft.getInstance());
        TintCoalescer.tick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        ReloadTimer.drawDone(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void onGuiRender(RenderGuiEvent.Post event) {
        if (Minecraft.getInstance().screen == null) ReloadTimer.drawDone(event.getGuiGraphics());
    }

    /** Al salir del mundo no quedarse con referencias a entidades del mundo viejo. */
    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        RenderLimits.clear();
        DynamicLights.clear();
        TintCoalescer.clear();
    }
}
