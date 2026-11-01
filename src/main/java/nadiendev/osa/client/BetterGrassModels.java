package nadiendev.osa.client;

import nadiendev.osa.OptimizerSodiumAddon;
import nadiendev.osa.OsaConfig;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Map;

@EventBusSubscriber(modid = OptimizerSodiumAddon.MODID, value = Dist.CLIENT)
public final class BetterGrassModels {
    private BetterGrassModels() {}

    @SubscribeEvent
    public static void onBake(ModelEvent.ModifyBakingResult event) {
        Map<BlockState, BlockStateModel> models = event.getBakingResult().blockStateModels();
        for (Identifier id : OsaConfig.betterGrassBlocks()) {
            Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
            if (block == null) continue;
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                BlockStateModel model = models.get(state);
                if (model != null) models.put(state, new BetterGrassModel(model));
            }
        }
    }
}
