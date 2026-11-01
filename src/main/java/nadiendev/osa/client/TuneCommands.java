package nadiendev.osa.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class TuneCommands {
    private TuneCommands() {}

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("osatune")
                .executes(TuneCommands::info)
                .then(ClientCommands.literal("info").executes(TuneCommands::info))
                .then(profile("auto", TuneProfile.AUTO))
                .then(profile("low", TuneProfile.LOW))
                .then(profile("medium", TuneProfile.MEDIUM))
                .then(profile("high", TuneProfile.HIGH)));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> profile(String name, TuneProfile profile) {
        return ClientCommands.literal(name).executes(c -> run(c, profile));
    }

    private static int info(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("[OSA] " + AutoTune.describeHardware()));
        return 1;
    }

    private static int run(CommandContext<FabricClientCommandSource> context, TuneProfile profile) {
        String summary = AutoTune.apply(profile, Minecraft.getInstance(), true);
        context.getSource().sendFeedback(Component.literal("[OSA] " + summary));
        return 1;
    }
}
