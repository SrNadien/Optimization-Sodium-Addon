package nadiendev.osa.client;

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
public final class CullCommands {
    private CullCommands() {}

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("osacull")
                .executes(CullCommands::stats)
                .then(Commands.literal("reset").executes(CullCommands::reset)));
    }

    private static int stats(CommandContext<CommandSourceStack> context) {
        String text = "[OSA] Culling de entidades: " + EntityCulling.summary();
        context.getSource().sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context) {
        EntityCulling.reset();
        context.getSource().sendSuccess(() -> Component.literal("[OSA] Contadores de culling a cero."), false);
        return 1;
    }
}
