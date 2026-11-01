package nadiendev.osa.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import nadiendev.osa.OptimizerSodiumAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = OptimizerSodiumAddon.MODID, value = Dist.CLIENT)
public final class TuneCommands {
    private TuneCommands() {}

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("osatune")
                .executes(TuneCommands::info)
                .then(Commands.literal("info").executes(TuneCommands::info))
                .then(profile("auto", TuneProfile.AUTO))
                .then(profile("low", TuneProfile.LOW))
                .then(profile("medium", TuneProfile.MEDIUM))
                .then(profile("high", TuneProfile.HIGH)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> profile(String name, TuneProfile profile) {
        return Commands.literal(name).executes(c -> run(c, profile));
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("[OSA] " + AutoTune.describeHardware()), false);
        return 1;
    }

    private static int run(CommandContext<CommandSourceStack> context, TuneProfile profile) {
        String summary = AutoTune.apply(profile, Minecraft.getInstance(), true);
        context.getSource().sendSuccess(() -> Component.literal("[OSA] " + summary), false);
        return 1;
    }
}
