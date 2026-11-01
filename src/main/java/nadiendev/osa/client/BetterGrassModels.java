package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

public final class BetterGrassModels {
    private BetterGrassModels() {}

    public static void register() {
        ModelLoadingPlugin.register(context -> {
            Set<Block> blocks = new HashSet<>();
            for (Identifier id : OsaConfig.betterGrassBlocks()) {
                BuiltInRegistries.BLOCK.getOptional(id).ifPresent(blocks::add);
            }
            if (blocks.isEmpty()) return;
            context.modifyBlockModelAfterBake().register(ModelModifier.WRAP_PHASE, (BlockStateModel model, ModelModifier.AfterBakeBlock.Context modelContext) ->
                    blocks.contains(modelContext.state().getBlock()) ? new BetterGrassModel(model) : model);
        });
    }
}
