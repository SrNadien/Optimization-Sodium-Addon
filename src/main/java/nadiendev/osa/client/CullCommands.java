package nadiendev.osa.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public final class CullCommands {
    private CullCommands() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("osacull")
                .executes(CullCommands::stats)
                .then(ClientCommands.literal("reset").executes(CullCommands::reset)));
    }

    private static int stats(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("[OSA] Culling de entidades: " + EntityCulling.summary()));
        return 1;
    }

    private static int reset(CommandContext<FabricClientCommandSource> context) {
        EntityCulling.reset();
        context.getSource().sendFeedback(Component.literal("[OSA] Contadores de culling a cero."));
        return 1;
    }
}
