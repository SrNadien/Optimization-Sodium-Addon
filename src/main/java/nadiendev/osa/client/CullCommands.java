package nadiendev.osa.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public final class CullCommands {
    private CullCommands() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("osacull")
                .executes(CullCommands::stats)
                .then(ClientCommandManager.literal("reset").executes(CullCommands::reset)));
    }

    private static int stats(CommandContext<FabricClientCommandSource> context) {
        String text = "[OSA] Culling de entidades: " + EntityCulling.summary();
        context.getSource().sendFeedback(Component.literal(text));
        return 1;
    }

    private static int reset(CommandContext<FabricClientCommandSource> context) {
        EntityCulling.reset();
        context.getSource().sendFeedback(Component.literal("[OSA] Contadores de culling a cero."));
        return 1;
    }
}
