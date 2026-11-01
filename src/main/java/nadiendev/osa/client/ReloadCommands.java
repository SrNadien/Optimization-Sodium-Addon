package nadiendev.osa.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public final class ReloadCommands {
    private ReloadCommands() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("osareload")
                .executes(ReloadCommands::last)
                .then(toggle("on", true))
                .then(toggle("off", false)));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> toggle(String name, boolean enabled) {
        return ClientCommands.literal(name).executes(c -> set(c, enabled));
    }

    private static int last(CommandContext<FabricClientCommandSource> context) {
        String text = ReloadTimer.hasLast()
                ? "[OSA] Ultima recarga de recursos: " + ReloadTimer.last()
                : "[OSA] Todavia no hubo ninguna recarga de recursos.";
        context.getSource().sendFeedback(Component.literal(text));
        return 1;
    }

    private static int set(CommandContext<FabricClientCommandSource> context, boolean enabled) {
        ReloadTimer.setChat(enabled);
        String text = enabled
                ? "[OSA] Cada recarga de recursos mostrara su tiempo en el chat."
                : "[OSA] Las recargas ya no escriben su tiempo en el chat.";
        context.getSource().sendFeedback(Component.literal(text));
        return 1;
    }
}
