package nadiendev.osa.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import nadiendev.osa.OptimizerSodiumAddon;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = OptimizerSodiumAddon.MODID, value = Dist.CLIENT)
public final class ReloadCommands {
    private ReloadCommands() {}

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("osareload")
                .executes(ReloadCommands::last)
                .then(toggle("on", true))
                .then(toggle("off", false)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> toggle(String name, boolean enabled) {
        return Commands.literal(name).executes(c -> set(c, enabled));
    }

    private static int last(CommandContext<CommandSourceStack> context) {
        String text = ReloadTimer.hasLast()
                ? "[OSA] Ultima recarga de recursos: " + ReloadTimer.last()
                : "[OSA] Todavia no hubo ninguna recarga de recursos.";
        context.getSource().sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context, boolean enabled) {
        ReloadTimer.setChat(enabled);
        String text = enabled
                ? "[OSA] Cada recarga de recursos mostrara su tiempo en el chat."
                : "[OSA] Las recargas ya no escriben su tiempo en el chat.";
        context.getSource().sendSuccess(() -> Component.literal(text), false);
        return 1;
    }
}
