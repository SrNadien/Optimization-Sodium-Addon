package nadiendev.osa.client;

import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.OsaConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = OptimizerSodiumAddon.MODID, value = Dist.CLIENT)
public final class BetterGrassModels {
    private BetterGrassModels() {}

    @SubscribeEvent
    public static void onBake(ModelEvent.ModifyBakingResult event) {
        Set<Block> blocks = new HashSet<>();
        for (Identifier id : OsaConfig.betterGrassBlocks()) {
            BuiltInRegistries.BLOCK.getOptional(id).ifPresent(blocks::add);
        }
        if (blocks.isEmpty()) return;
        event.getBakingResult().blockStateModels().replaceAll((state, model) ->
                blocks.contains(state.getBlock()) ? new BetterGrassModel(model) : model);
    }
}
