package nadiendev.osa.client;

import nadiendev.osa.config.OsaConfig;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

public final class BetterGrassModels implements ModelLoadingPlugin {
    @Override
    public void initialize(Context context) {
        Set<Block> targets = new HashSet<>();
        for (Identifier id : OsaConfig.betterGrassBlocks()) {
            BuiltInRegistries.BLOCK.getOptional(id).ifPresent(targets::add);
        }
        if (targets.isEmpty()) return;
        context.modifyBlockModelAfterBake().register((model, modelContext) ->
                targets.contains(modelContext.state().getBlock()) ? new BetterGrassModel(model) : model);
    }
}
