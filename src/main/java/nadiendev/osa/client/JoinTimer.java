package nadiendev.osa.client;

import nadiendev.osa.OptimizerSodiumAddon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = OptimizerSodiumAddon.MODID, value = Dist.CLIENT)
public final class JoinTimer {
    private static long connectStart = -1;
    private static long phaseStart = -1;
    private static long recipesStart = -1;
    private static long tagsStart = -1;
    private static boolean waitingForWorld = false;
    private static final List<String> lines = new ArrayList<>();

    private JoinTimer() {}

    private static long now() {
        return System.nanoTime();
    }

    private static String secs(long nanos) {
        return String.format("%.1f s", nanos / 1_000_000_000.0);
    }

    private static void mark(String label, long nanos) {
        lines.add(label + ": " + secs(nanos));
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof ConnectScreen && connectStart < 0) {
            connectStart = now();
            phaseStart = connectStart;
            lines.clear();
        }
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        long t = now();
        if (connectStart < 0) {
            connectStart = t;
        } else {
            mark("Conexion + login + sincronizar registros", t - phaseStart);
        }
        phaseStart = t;
        waitingForWorld = true;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRecipesFirst(RecipesReceivedEvent event) {
        recipesStart = now();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRecipesLast(RecipesReceivedEvent event) {
        if (recipesStart > 0 && connectStart > 0) {
            mark("Mods procesando recetas (JEI, libro de recetas, etc.)", now() - recipesStart);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTagsFirst(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
            tagsStart = now();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTagsLast(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED && tagsStart > 0 && connectStart > 0) {
            mark("Mods procesando tags", now() - tagsStart);
        }
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        if (!waitingForWorld) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        long t = now();
        mark("Desde login hasta poder jugar (chunks, quests, resto)", t - phaseStart);
        long total = t - connectStart;
        waitingForWorld = false;

        OptimizerSodiumAddon.LOG.info("[OSA] ===== Tiempo de entrada al servidor: {} =====", secs(total));
        for (String l : lines) OptimizerSodiumAddon.LOG.info("[OSA]   {}", l);

        mc.player.displayClientMessage(Component.literal("[OSA] Entraste en " + secs(total) + " (detalle en logs/latest.log)")
                .withStyle(ChatFormatting.AQUA), false);
        for (String l : lines) {
            mc.player.displayClientMessage(Component.literal("  " + l).withStyle(ChatFormatting.GRAY), false);
        }

        connectStart = -1;
        recipesStart = -1;
        tagsStart = -1;
        lines.clear();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        connectStart = -1;
        recipesStart = -1;
        tagsStart = -1;
        waitingForWorld = false;
        lines.clear();
    }
}
